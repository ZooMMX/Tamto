package hello;

import hello.exporter.PiezasExcelView;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

    @Autowired
    View piezasExcelView;

    @Autowired
    TamtoPermissionEvaluator tamtoPermissionEvaluator;

    @RequestMapping("/piezas")
    public String pieza (
        Model model,
        @RequestParam(required = false, defaultValue = "false") Boolean successfulChange
    ) {
        model.addAttribute("selectedMenu", "piezas");
        model.addAttribute("successfulChange", successfulChange);
        return "piezas";
    }

    @RequestMapping(value = "/piezas/export")
        public ModelAndView getExport(Model model) {
        List<Pieza> piezaList = (List<Pieza>) piezaRepository.findAll();
        model.addAttribute("piezas", piezaList);
        return new ModelAndView(piezasExcelView, model.asMap());
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
            HttpServletRequest request,
            Model model) {
                HashMap resp = new HashMap<String, Object>();

                /* Ejecución de acciones */
                if(customActionType != null && customActionType.equals("group_action"))
                    // ** ¿Borrar? ** sólo si es administrador
                    if(customActionName != null && customActionName.equals("softdelete")
                            && id != null
                            && request.isUserInRole("ROLE_ADMIN_PLANEACION")) {
                        ocultarPiezas(id);
                        resp.put("softdeleted", true); //Señal para tests y msgs al usuario
                    } else {
                        resp.put("softdeleted", false);//Señal para tests y msgs al usuario
                    }
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

                /* ** Ordenación ** */
                piezaQuery.orderBy(qb.desc(p.get(Pieza_.id)));

                /* ** Paginación y ejecución de consulta ** */
                TypedQuery<Pieza> piezasTypedQuery = em.createQuery( piezaQuery );
                piezasTypedQuery.setFirstResult(start);
                if(length > 0) piezasTypedQuery.setMaxResults(length); //Infinito si length es menor o igual a cero
                List<Pieza> piezas = piezasTypedQuery.getResultList(); //Ejecución de la consulta maestra
                CriteriaQuery countQuery = piezaQuery;
                countQuery.select( qb.count( p.get(Pieza_.id) ) );
                Long piezasFiltradas = (Long) em.createQuery( countQuery ).getSingleResult();   //Ejecución de la consulta conteo de elementos filtrados

                //Agrega atributos HTML a la entidad
                enhancePiezasForView(piezas, request);

                /* Renderizar resultado */
                //model.addAttribute("name", p.toString());
                model.addAttribute("name", "¿nombre?");
                resp.put("data", piezas);
                resp.put("draw", draw);
                resp.put("recordsTotal", piezaRepository.countEnabled());
                resp.put("recordsFiltered", piezasFiltradas);
                return resp;
        }

    @RequestMapping(value = "/piezas/find")
    public @ResponseBody List<Pieza> piezasFindJSON(
            @RequestParam Integer length,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(required = false) String descripcion_filter,
            @RequestParam(required = false) String nombre_sap_filter,
            @RequestParam(required = false) String codigo_filter,
            HttpServletRequest request,
            Model model) {
        HashMap resp = new HashMap<String, Object>();

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
        Predicate condicionCodigo = null;
        if(codigo_filter != null && codigo_filter.length() > 0) {
            condicionCodigo = qb.like(p.get(Pieza_.universalCode), '%'+codigo_filter+'%');
        }

        // ** Filtro de descripción **
        Predicate condicionDescripcion = null;
        if(descripcion_filter != null && descripcion_filter.length() > 0) {
            condicionDescripcion = qb.like(p.get(Pieza_.descripcion), '%'+descripcion_filter+'%');
        }

        // Código AND descripción
        if(condicionCodigo!=null && condicionDescripcion!=null)
            condiciones = qb.and(condiciones, qb.or(condicionCodigo, condicionDescripcion));
        else {
            if(condicionCodigo!=null) condiciones = qb.and(condiciones, condicionCodigo);
            if(condicionDescripcion!=null) condiciones = qb.and(condiciones, condicionDescripcion);
        }

        piezaQuery.where(condiciones);
        /* **** Terminan filtros *** */

        /* ** Paginación y ejecución de consulta ** */
        TypedQuery<Pieza> piezasTypedQuery = em.createQuery( piezaQuery );
        piezasTypedQuery.setFirstResult(page*length);
        if(length > 0) piezasTypedQuery.setMaxResults(length); //Infinito si length es menor o igual a cero
        List<Pieza> piezas = piezasTypedQuery.getResultList(); //Ejecución de la consulta maestra
        CriteriaQuery countQuery = piezaQuery;
        countQuery.select( qb.count( p.get(Pieza_.id) ) );
        Long piezasFiltradas = (Long) em.createQuery( countQuery ).getSingleResult();   //Ejecución de la consulta conteo de elementos filtrados

        //Agrega atributos HTML a la entidad
        enhancePiezasForView(piezas, request);

        /* Renderizar resultado */
        return piezas;
    }

    private List<PiezaHtmlHelper> enhancePiezasForView(List<Pieza> docs, HttpServletRequest request) {
        List<PiezaHtmlHelper> lista = new ArrayList();

        for (Pieza di : docs) {
           PiezaHtmlHelper htmlHelper = new PiezaHtmlHelper();
           di.setHtmlHelper(htmlHelper);
           htmlHelper.setPermissionEvaluator(tamtoPermissionEvaluator);
           htmlHelper.setHttpServletRequest(request);
        }

        return lista;
    }

    @Transactional
    private void ocultarPiezas(Long[] id) {
        for (Long aLong : id) {
            Pieza p = piezaRepository.findOne(aLong);
            p.setEnabled(false);
            piezaRepository.save(p);
        }
    }
}
