package hello;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Proyecto Omoikane: SmartPOS 2.0
 * User: octavioruizcastillo
 * Date: 22/11/14
 * Time: 13:00
 */
@Controller
public class PiezasController {
    @Autowired
    PiezaRepository piezaRepository;

    @Autowired
    EntityManager em ;

    @RequestMapping("/piezas")
    public String pieza (
        Model model,
        @RequestParam(required = false, defaultValue = "false") Boolean successfulChange
    ) {
        model.addAttribute("selectedMenu", "piezas");
        model.addAttribute("successfulChange", successfulChange);
        return "piezas";
    }

    @RequestMapping(value = "/piezasJSON")
        public @ResponseBody HashMap<String, Object> piezasJSON(
            @RequestParam Integer length,
            @RequestParam Integer start,
            @RequestParam Integer draw,
            @RequestParam(required = false) String descripcion_filter,
            @RequestParam(required = false) TipoPieza tipo_pieza_filter,
            @RequestParam(required = false) String cliente_filter,
            @RequestParam(required = false) String nombre_sap_filter,
            @RequestParam(required = false) String codigo_filter,
            @RequestParam(required = false) Long order_no_from,
            @RequestParam(required = false) Long order_no_to,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") Date order_date_from,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") Date order_date_to,
            /* customActionType, customActionName e id son campos de la implementación de la acción Archivar/Ocultar/Softdelete */
            @RequestParam(required = false) String customActionType,
            @RequestParam(required = false) String customActionName,
            @RequestParam(value = "id[]", required = false) Long[] id,
            Model model) {
                HashMap resp = new HashMap<String, Object>();
                Integer pageNo = start / length;

                /* Ejecución de acciones */
                if(customActionType != null && customActionType.equals("group_action"))
                    // ** ¿Borrar? **
                    if(customActionName != null && customActionName.equals("softdelete") && id != null) ocultarPiezas(id);
                /* Terminan acciones */

                /* Comienza filtros mediante criteria builder de JPA */
                CriteriaBuilder qb = em.getCriteriaBuilder();
                CriteriaQuery<Pieza> piezaQuery = qb.createQuery(Pieza.class);
                Root<Pieza> p = piezaQuery.from(Pieza.class);
                //Establezco una condición notNull únicamente para inicializar las condiciones
                Predicate condiciones = qb.isNotNull(p.get(Pieza_.id));

                // ** Filtro de piezas activas **
                if(true) {
                    Predicate condicion = qb.equal(p.get(Pieza_.enabled), true);
                    condiciones = qb.and(condiciones, condicion);
                }

                // ** Filtro de código universal **
                if(codigo_filter != null && codigo_filter.length() > 0) {
                    Predicate condicion = qb.like(p.get(Pieza_.universalCode), '%'+codigo_filter+'%');
                    condiciones = qb.and(condiciones, condicion);
                }

                // ** Filtro de descripción **
                if(descripcion_filter != null && descripcion_filter.length() > 0) {
                    Predicate condicion = qb.like(p.get(Pieza_.descripcion), '%'+descripcion_filter+'%');
                    condiciones = qb.and(condiciones, condicion);
                }

                // ** Filtro de tipo **
                if(tipo_pieza_filter != null) {
                    Predicate condicion = qb.equal(p.get(Pieza_.tipoPieza), tipo_pieza_filter);
                    condiciones = qb.and(condiciones, condicion);
                }

                // ** Filtro de cliente **
                if(cliente_filter != null && cliente_filter.length() > 0) {
                    Predicate condicion = qb.like(p.get(Pieza_.cliente), '%'+cliente_filter+'%');
                    condiciones = qb.and(condiciones, condicion);
                }

                // ** Filtro de nombre SAP **
                if(nombre_sap_filter != null && nombre_sap_filter.length() > 0) {
                    Predicate condicion = qb.like(p.get(Pieza_.nombreSap), '%'+nombre_sap_filter+'%');
                    condiciones = qb.and(condiciones, condicion);
                }

                // ** Filtro de número de orden de compra **
                if(order_no_from != null && order_no_to != null) {
                    Predicate condicionFrom = qb.greaterThanOrEqualTo( p.get(Pieza_.workOrderNo), order_no_from );
                    Predicate condicionTo   = qb.lessThanOrEqualTo   ( p.get(Pieza_.workOrderNo), order_no_to);
                    condiciones = qb.and(condiciones, condicionFrom, condicionTo);
                }

                // ** Filtro de fecha de orden de compra **
                if(order_date_from != null && order_date_to != null) {
                    /* Utilizo la función "date" de MySQL para filtrar únicamente por fecha, sin considerar hora. */
                    Predicate condicionFrom = qb.greaterThanOrEqualTo( qb.function("date", Date.class, p.get(Pieza_.workOrderDate) ), order_date_from);
                    Predicate condicionTo   = qb.lessThanOrEqualTo(    qb.function("date", Date.class, p.get(Pieza_.workOrderDate) ), order_date_to);
                    condiciones = qb.and(condiciones, condicionFrom, condicionTo);
                }

                piezaQuery.where(condiciones);
                /* **** Terminan filtros *** */

                /* ** Paginación y ejecución de consulta ** */
                TypedQuery<Pieza> piezasTypedQuery = em.createQuery( piezaQuery );
                piezasTypedQuery.setFirstResult(start);
                piezasTypedQuery.setMaxResults(length);
                List<Pieza> piezas = piezasTypedQuery.getResultList(); //Ejecución de la consulta maestra
                CriteriaQuery countQuery = piezaQuery;
                countQuery.select( qb.count( p.get(Pieza_.id) ) );
                Long piezasFiltradas = (Long) em.createQuery( countQuery ).getSingleResult();   //Ejecución de la consulta conteo de elementos filtrados

                /* Renderizar resultado */
                //model.addAttribute("name", p.toString());
                model.addAttribute("name", "¿nombre?");
                resp.put("data", piezas);
                resp.put("draw", draw);
                resp.put("recordsTotal", piezaRepository.countEnabled());
                resp.put("recordsFiltered", piezasFiltradas);
                return resp;
        }

    @Transactional
    private void ocultarPiezas(Long[] id) {
        for (Long aLong : id) {
            Pieza p = piezaRepository.findOne(aLong);
            p.setEnabled(false);
            piezaRepository.save(p);
        }
        System.out.println(id);
    }
}
