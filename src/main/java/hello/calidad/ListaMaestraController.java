package hello.calidad;

import hello.*;
import hello.util.CloudConvertConnector;
import hello.util.NullAwareBeanUtilsBean;
import org.aioobe.cloudconvert.CloudConvertService;
import org.aioobe.cloudconvert.ConvertProcess;
import org.aioobe.cloudconvert.ProcessStatus;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.rowset.serial.SerialException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.security.Principal;
import java.sql.Blob;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

/**
 * Proyecto tamto
 * User: octavioruizcastillo
 * Date: 04/05/15
 * Time: 8:13
 */
@Controller
public class ListaMaestraController {

    @Autowired
    ListaMaestraRepository listaMaestraRepository;

    @Autowired
    EntityManager em;

    @Autowired
    TamtoPermissionEvaluator tamtoPermissionEvaluator;

    @PreAuthorize("hasPermission('listamaestra', 'VER')")
    @RequestMapping({"/calidad/listaMaestra", "/calidad"})
    public String listaMaestra(Model model) {

        List<DocumentoInterno> docsSinAprobar = listaMaestraRepository.findDocsSinAprobar();

        model.addAttribute("docsSinAprobar", docsSinAprobar);

        return "calidad/lista_maestra";
    }

    /**
     * Sirve para llenar la datatable de la lista maestra de documentos internos del sistema de calidad.
     * Tiene las siguientes responsabilidades:
     *  - Se encarga de ocultar documentos internos
     *  - Se encarga de filtrar por cualquier campo
     *  - Maneja la paginación
     * @param length
     * @param start
     * @param draw
     * @param filter_nivel
     * @param filter_tipo
     * @param filter_codigo
     * @param filter_titulo
     * @param filter_proxima_revision_from
     * @param filter_proxima_revision_to
     * @param filter_departamento
     * @param customActionType
     * @param customActionName
     * @param id
     * @param request
     * @param model
     * @return
     */

    @PreAuthorize("hasPermission('listamaestra', 'VER')")
    @RequestMapping("/calidad/listaMaestraJSON")
    @ResponseBody public HashMap<String, Object> getJSON(
            @RequestParam Integer length,
            @RequestParam Integer start,
            @RequestParam Integer draw,
            @RequestParam(required = false) Integer filter_nivel,
            @RequestParam(required = false) TipoDocumento filter_tipo,
            @RequestParam(required = false) String filter_codigo,
            @RequestParam(required = false) String filter_titulo,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") Date filter_proxima_revision_from,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") Date filter_proxima_revision_to,
            @RequestParam(required = false) Departamento filter_departamento,
            /* customActionType, customActionName e id son campos de la implementación de la acción Archivar/Ocultar/Softdelete */
            @RequestParam(required = false) String customActionType,
            @RequestParam(required = false) String customActionName,
            @RequestParam(value = "id[]", required = false) Long[] id,
            HttpServletRequest request,
            Model model
    ) {

        /* --- Ejecución de acciones --- */
        if(customActionType != null && customActionType.equals("group_action"))
            // ** ¿Borrar? ** sólo si es administrador
            if(customActionName != null && customActionName.equals("softdelete")
                    && id != null
                    && request.isUserInRole("ROLE_ADMIN_CALIDAD"))
                ocultarDocumentos(id);
        /* --- Terminan acciones --- */

        /* Comienza filtros mediante criteria builder de JPA */
        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<DocumentoInterno> documentoQuery = qb.createQuery(DocumentoInterno.class);
        Root<DocumentoInterno> p = documentoQuery.from(DocumentoInterno.class);
        //Establezco una condición notNull únicamente para inicializar las condiciones
        Predicate condiciones = qb.isNotNull(p.get(DocumentoInterno_.id));

        // ** Filtro de documentos activos **
        if(true) {
            Predicate condicion = qb.equal(p.get(DocumentoInterno_.enabled), true);
            condiciones = qb.and(condiciones, condicion);
        }

        // ** Filtro de documentos aprobados **
        if(true) {
            Predicate condicion = qb.equal(p.get(DocumentoInterno_.aprobado), true);
            condiciones = qb.and(condiciones, condicion);
        }

        // ** Filtro de nivel **
        if(filter_nivel != null) {
            Predicate condicion = qb.equal(p.get(DocumentoInterno_.nivel), filter_nivel);
            condiciones = qb.and(condiciones, condicion);
        }

        // ** Filtro de tipo **
        if(filter_tipo != null) {
            Predicate condicion = qb.equal(p.get(DocumentoInterno_.tipo), filter_tipo);
            condiciones = qb.and(condiciones, condicion);
        }

        // ** Filtro de código **
        if(filter_codigo != null && filter_codigo.length() > 0) {
            Predicate condicion = qb.like(p.get(DocumentoInterno_.codigo), '%'+filter_codigo+'%');
            condiciones = qb.and(condiciones, condicion);
        }

        // ** Filtro de título **
        if(filter_titulo != null && filter_titulo.length() > 0) {
            Predicate condicion = qb.like(p.get(DocumentoInterno_.titulo), '%'+filter_titulo+'%');
            condiciones = qb.and(condiciones, condicion);
        }
		/*
        // ** Filtro de fecha de elaboración **
        if(filter_fecha_elaboracion_from != null && filter_fecha_elaboracion_to != null) {
            // Utilizo la función "date" de MySQL para filtrar únicamente por fecha, sin considerar hora.
            Predicate condicionFrom = qb.greaterThanOrEqualTo( qb.function("date", Date.class, p.get(DocumentoInterno_.fechaElaboracion) ), filter_fecha_elaboracion_from);
            Predicate condicionTo   = qb.lessThanOrEqualTo(    qb.function("date", Date.class, p.get(DocumentoInterno_.fechaElaboracion) ), filter_fecha_elaboracion_to);
            condiciones = qb.and(condiciones, condicionFrom, condicionTo);
        }

        // ** Filtro de fecha de última aprobación **
        if(filter_ultima_aprobacion_from != null && filter_ultima_aprobacion_to != null) {
            // Utilizo la función "date" de MySQL para filtrar únicamente por fecha, sin considerar hora.
            Predicate condicionFrom = qb.greaterThanOrEqualTo( qb.function("date", Date.class, p.get(DocumentoInterno_.ultimaAprobacion) ), filter_ultima_aprobacion_from);
            Predicate condicionTo   = qb.lessThanOrEqualTo(    qb.function("date", Date.class, p.get(DocumentoInterno_.ultimaAprobacion) ), filter_ultima_aprobacion_to);
            condiciones = qb.and(condiciones, condicionFrom, condicionTo);
        }
		*/
        // ** Filtro de fecha de próxima revisión **
        if(filter_proxima_revision_from != null && filter_proxima_revision_to != null) {
            // Utilizo la función "date" de MySQL para filtrar únicamente por fecha, sin considerar hora.
            Predicate condicionFrom = qb.greaterThanOrEqualTo( qb.function("date", Date.class, p.get(DocumentoInterno_.proximaRevision) ), filter_proxima_revision_from);
            Predicate condicionTo   = qb.lessThanOrEqualTo(    qb.function("date", Date.class, p.get(DocumentoInterno_.proximaRevision) ), filter_proxima_revision_to);
            condiciones = qb.and(condiciones, condicionFrom, condicionTo);
        }
        // ** Filtro de departamento **
        if(filter_departamento != null) {
            Predicate condicion = qb.equal(p.get(DocumentoInterno_.departamento), filter_departamento);
            condiciones = qb.and(condiciones, condicion);
        }

        documentoQuery.where(condiciones);
        /* **** Terminan filtros *** */

        /* ** Paginación y ejecución de consulta ** */
        TypedQuery<DocumentoInterno> docTypedQuery = em.createQuery( documentoQuery );
        docTypedQuery.setFirstResult(start);
        if(length > 0) docTypedQuery.setMaxResults(length); //Infinito si length es menor o igual a cero
        List<DocumentoInterno> docs = docTypedQuery.getResultList(); //Ejecución de la consulta maestra
        CriteriaQuery countQuery = documentoQuery;
        countQuery.select( qb.count( p.get(DocumentoInterno_.id) ) );
        Long docsFiltrados = (Long) em.createQuery( countQuery ).getSingleResult();   //Ejecución de la consulta conteo de elementos filtrados

        // Puesta a punto de la vista
        HashMap resp = new HashMap<String, Object>();

        //Agrega atributos HTML a la entidad
        enhanceDocsForView(docs, request);

        //Prepara la respuesta
        resp.put("data", docs);
        resp.put("draw", draw);
        resp.put("recordsTotal", listaMaestraRepository.countEnabled());
        resp.put("recordsFiltered", docsFiltrados);
        return resp;
    }

    private List<DocumentoInternoHtmlHelper> enhanceDocsForView(List<DocumentoInterno> docs, HttpServletRequest request) {
        List<DocumentoInternoHtmlHelper> lista = new ArrayList();

        for (DocumentoInterno di : docs) {
            DocumentoInternoHtmlHelper htmlHelper = new DocumentoInternoHtmlHelper();
            di.setHtmlHelper(htmlHelper);
            htmlHelper.setPermissionEvaluator(tamtoPermissionEvaluator);
            htmlHelper.setHttpServletRequest(request);
        }

        return lista;
    }

    @Transactional
    private void ocultarDocumentos(Long[] id) {
        for (Long aLong : id) {
            DocumentoInterno doc = listaMaestraRepository.findOne(aLong);
            doc.setEnabled(false);
            listaMaestraRepository.save(doc);
        }
    }

    @PreAuthorize("hasPermission(#id, 'listamaestra', 'AGREGAR')")
    @RequestMapping("/calidad/documento/nuevo")
    public String nuevoDoc(Model model) {

        DocumentoInterno di = new DocumentoInterno();
        model.addAttribute("documento", di);
        model.addAttribute("modoEdicion", false); //Indica que no se está editando, si no creando
        model.addAttribute("departamentos", joinDepartamentosAndRoles());

        return "calidad/documento_nuevo_editar";
    }

    //Hace match entre roles del usuario y departamentos disponibles. Así genera una lista de departamentos diponibles para el usuario.
    private List<Departamento> joinDepartamentosAndRoles() {

        List<Departamento> depts = new ArrayList<>();
        for(GrantedAuthority ga : SecurityContextHolder.getContext().getAuthentication().getAuthorities())
            for(Departamento d : Departamento.values())
                if(ga.getAuthority().equalsIgnoreCase("ROLE_"+d.toString()))
                    depts.add(d);

        return depts;
    }

    @PreAuthorize("hasPermission(#id, 'listamaestra', 'EDITAR')")
    @RequestMapping("/calidad/documento/editar/{id}")
    public String nuevoDoc(
            @PathVariable Long id, Model model) {
        DocumentoInterno di = listaMaestraRepository.findOne(id);

        model.addAttribute("documento", di);
        model.addAttribute("modoEdicion", true); //Indica que se está editando el documento
        model.addAttribute("departamentos", joinDepartamentosAndRoles());

        return "calidad/documento_nuevo_editar";
    }

    @PreAuthorize("hasPermission(#id, 'listamaestra', 'EDITAR')")
    @RequestMapping(value = "/calidad/documento", method = RequestMethod.POST)
    public String nuevoDocPOST(
            Model model,
            @ModelAttribute DocumentoInterno documento,
            @RequestParam("archivo") MultipartFile[] archivo,
            @RequestParam("documento_editable") MultipartFile[] documentoEditable,
            Principal principal,
            RedirectAttributes redirectAttrs) throws InvocationTargetException, IllegalAccessException {
        //Flag whether it is a new entity being saved or an existing entity being updated
        Boolean isNew = documento.getId() == null;

        //Initialize unpassed fields
        if(!isNew) {
            DocumentoInterno docCompleto = listaMaestraRepository.findOne(documento.getId());
            NullAwareBeanUtilsBean notNull = new NullAwareBeanUtilsBean();
            notNull.copyProperties(docCompleto, documento);
            documento = docCompleto;
        }


        //File treatment (Editable file)
        MultipartFile documentoFuente = null;
        if(documentoEditable.length > 0 && documentoEditable[0] != null) {
            try {
                MultipartFile f2 = documentoEditable[0];

                //Si el archivo está vacío, se lo salta
                if(f2.isEmpty()) throw new Exception("Documento fuente vacío");
                documentoFuente = f2;

                Blob blob = new javax.sql.rowset.serial.SerialBlob(f2.getBytes());
                documento.setEditableFileName(f2.getOriginalFilename());
                documento.setEditableFileSize(f2.getSize());
                documento.setEditableFileType(f2.getContentType());
                documento.setDocumentoEditable(blob);

            } catch (Exception e) {
                //Lanzar mensaje de error, pero después continuar para guardar los datos del documento
                if(isNew) {
                    redirectAttrs.addAttribute("errorMsg", e.getMessage() + ", la información del documento se intentará guardar aunque sin archivo adjunto");
                    e.printStackTrace();
                }
            }
        }

        //File treatment (PDF preview file)
        if(documentoFuente != null) {
            byte[] pdf;
            try {
                //Se convierte el documento original en un PDF
                pdf = toPDF(documentoFuente);
                //Defino el nombre del PDF como el nombre original pero con extensión PDF
                String nombreDestino = FilenameUtils.getBaseName(documentoFuente.getOriginalFilename())+".pdf";
                //Establezco todos los metadatos y el archivo en si mismo
                Blob blob = new javax.sql.rowset.serial.SerialBlob(pdf);
                documento.setFileName(nombreDestino);
                documento.setFileSize((long) pdf.length);
                documento.setFileType("application/pdf");
                documento.setDocumento(blob);
            } catch (URISyntaxException | InterruptedException | ParseException | SQLException | IOException e) {
                redirectAttrs.addAttribute("errorMsg", e.getMessage() + ", ocurrió un error generando vista previa del archivo");
                e.printStackTrace();
            }
        }

        //Determine ROLCS based on Departamento (ROLCS is totally equal to Departamento)
        documento.setRolcs(ROLCS.valueOf(documento.getDepartamento().toString()));

        //Entity save
        listaMaestraRepository.save(documento);

        //Redirect success message
        redirectAttrs.addAttribute("successMsg", "Atributos del documento registrados correctamente");

        return "redirect:/calidad/listaMaestra";
    }

    private byte[] toPDF(MultipartFile origen) throws URISyntaxException, IOException, ParseException, InterruptedException {
        CloudConvertConnector connector = new CloudConvertConnector();
        return connector.toPDF(origen);
    }

    @PreAuthorize("hasPermission(#id, 'listamaestra', 'VER')")
    @RequestMapping("/calidad/documento/{rev}/{id}")
    public String verDoc(@PathVariable Integer rev, @PathVariable Long id, Model model) {

        //Si rev=0 entonces considero que se quiere la copia actual y no una revisión anterior
        DocumentoInterno documento;
        if(rev == 0)
            documento = listaMaestraRepository.findOne(id);
        else {
            AuditReader reader = AuditReaderFactory.get(em);
            documento = reader.find(DocumentoInterno.class, id, rev);
        }

        List<Object> versiones = listaMaestraRepository.getVersiones(id);

        model.addAttribute("documento", documento);
        model.addAttribute("revision" , rev);
        model.addAttribute("versiones", versiones);

        return "calidad/documento";
    }

    @PreAuthorize("hasPermission(#id, 'listamaestra', 'VER')")
    @RequestMapping("/calidad/documento/{rev}/{id}.pdf")
    public String verDocPDF(@PathVariable("id") Long id,
                            @PathVariable("rev") Integer rev,
                            HttpServletResponse response) throws SQLException, IOException {

        DocumentoInterno documento;
        // la variable "rev" indica que se quiere rescatar de la revisión #"rev" anterior, si rev=0 -> se toma el PDF actual
        if(rev == 0)
            documento = listaMaestraRepository.findOne(id);
        else {
            AuditReader reader = AuditReaderFactory.get(em);
            documento = reader.find(DocumentoInterno.class, id, rev);
        }
        try {
            response.setHeader("Content-Disposition", "inline; filename=\"" +documento.getFileName()+ ".pdf\"");
            response.setContentType("application/pdf");
            response.setHeader("Content-Transfer-Encoding", "binary");
            response.setContentLength((int) documento.getDocumento().length());
            OutputStream out = response.getOutputStream();
            IOUtils.copy(documento.getDocumento().getBinaryStream(), out);
            out.flush();
            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }


        return null;
    }

    /**
     * Método para descargar los documentos PDF
     * @param id Del documento interno
     * @param response
     * @return
     */
    @PreAuthorize("hasPermission(#id, 'listamaestra', 'DESCARGAR')")
    @RequestMapping("/calidad/documento/{rev}/{id}/descargar/pdf")
    public String downloadPDF(
            @PathVariable("rev") Integer rev,
            @PathVariable("id")
            Long id, HttpServletResponse response) {

        //Si rev == 0 entonces tomo la versión actual del documento, si no tomo la revisión dada
        DocumentoInterno di;
        if (rev == 0)
            di = listaMaestraRepository.findOne(id);
        else {
            AuditReader reader = AuditReaderFactory.get(em);
            di = reader.find(DocumentoInterno.class, id, rev);
        }
        try {
            response.setHeader("Content-Disposition", "attachment; filename=\"" +di.getFileName()+ "\"");
            response.setContentType("application/force-download");
            response.setHeader("Content-Transfer-Encoding", "binary");
            response.setContentLength((int) di.getDocumento().length());
            OutputStream out = response.getOutputStream();
            IOUtils.copy(di.getDocumento().getBinaryStream(), out);
            out.flush();
            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Método para descargar los documentos fuente, también los he llamado a veces documentos editables para denotar su diferencia con los PDF que utilizamos como formato
     * inmutable.
     * @param id Del documento interno
     * @param response
     * @return
     */
    @PreAuthorize("hasPermission(#id, 'listamaestra', 'DESCARGAR')")
    @RequestMapping("/calidad/documento/{rev}/{id}/descargar/fuente")
    public String downloadSource(
            @PathVariable("rev") Integer rev,
            @PathVariable("id")
            Long id, HttpServletResponse response) {

        //Si rev == 0 entonces tomo la versión actual del documento, si no tomo la revisión dada
        DocumentoInterno di;
        if (rev == 0)
            di = listaMaestraRepository.findOne(id);
        else {
            AuditReader reader = AuditReaderFactory.get(em);
            di = reader.find(DocumentoInterno.class, id, rev);
        }
        try {
            response.setHeader("Content-Disposition", "attachment; filename=\"" +di.getEditableFileName()+ "\"");
            response.setContentType("application/force-download");
            response.setHeader("Content-Transfer-Encoding", "binary");
            response.setContentLength((int) di.getDocumentoEditable().length());
            OutputStream out = response.getOutputStream();
            IOUtils.copy(di.getDocumentoEditable().getBinaryStream(), out);
            out.flush();
            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    @PreAuthorize("hasPermission(#id, 'listamaestra', 'VER')")
    @RequestMapping("/calidad/documento/{id}/auditoria")
    public String verAuditoria(@PathVariable("id") Long id, Model model) {

        model.addAttribute("revisiones", createRevisionsDocumentoInterno(id));

        return "calidad/auditoria";
    }

    /**
     * Genera una lista de auditoría con los últimos cambios realizados a un documento, características:
     *  - Especifica quien realizó el cambio
     *  - Especifica cuando se realizó
     *  - Especifica cada campo que fue modificado
     *  - Especifica el nuevo valor de cada campo modificado
     *  - Sólo considera las últimas 100 revisiones
     *  - Siempre agrega el autor y la fecha de creación del registro (1ra revisión) en el último elemento de la lista
     * @param documentoId
     * @return
     */
    private List<ItemAuditoria> createRevisionsDocumentoInterno(Long documentoId) {

        // ******** Datos sobre la creación de la entidad (primera revisión) ******
        Query q0 = em.createNativeQuery("SELECT u.fullname, timestamp FROM documento_interno_aud doc JOIN revision r ON doc.rev = r.id JOIN user u ON r.username = u.username where doc.id = "+documentoId+" ORDER BY timestamp ASC LIMIT 1");
        Object[] firstRev = (Object[]) q0.getSingleResult();
        //La fecha de creación también puede ser tomada directo de DocumentoInterno.created
        Date created = (Date) Date.from(Instant.ofEpochMilli(((BigInteger) firstRev[1]).longValue()));
        String autor = (String) firstRev[0];

        // ******** Datos sobre actualizaciones a la entidad *******
        //  (Índices del array)--------->      [0]     [1]    [2]   [3]     [4]    [5]     [6]               [7]      [8]           [9]          [10]       [11]       [12]      [13]        [14]       [15]        [16]                  [17]         [18]              [19]           [20]       [21]         [22]           [23]        [24]       [25]
        Query q = em.createNativeQuery("SELECT doc.id, nivel, tipo, codigo, rolcs, titulo, proxima_revision, version, departamento, doc.enabled, file_name, nivel_mod, tipo_mod, codigo_mod, rolcs_mod, titulo_mod, proxima_revision_mod, version_mod, departamento_mod, documento_mod, notas_mod, enabled_mod, file_name_mod, u.fullname, timestamp, documento_editable_mod FROM documento_interno_aud doc JOIN revision r ON doc.rev = r.id JOIN user u ON r.username = u.username where doc.id = "+documentoId+" ORDER BY timestamp ASC LIMIT 100 OFFSET 1");
        List rl = q.getResultList();
        //Hago SELECT en orden inverso para poder utilizar OFFSET y quitar el registro de creación de la entidad
        //Con Collections.reverse pongo la lista en el sentido que requiero de más actual a más viejo
        Collections.reverse(rl);

        List<ItemAuditoria> resultados = new ArrayList<>();

        for(Object object : rl) {
            Object[] r = (Object[]) object;
            // Mapeo manual de campos
            Integer nivel = (Integer) r[1];
            TipoDocumento tipo = r[2] == null ? null : TipoDocumento.valueOf((String) r[2]);
            String codigo = (String) r[3];
            ROLCS rolcs = r[4] == null ? null : ROLCS.valueOf((String) r[4]);
            String titulo = (String) r[5];
            Date proximaRevision = (Date) r[6];
            String version = (String) r[7];
            String departamento = (String) r[8];
            // No obtengo el documento por ser un blob
            // No obtengo las notas por ser muy largas para mostrar a detalle
            Boolean enabled = (Boolean) r[9];
            String fileName = (String) r[10];
            Boolean nivelMod = (Boolean) r[11];
            Boolean tipoMod = (Boolean) r[12];
            Boolean codigoMod = (Boolean) r[13];
            Boolean rolcsMod = (Boolean) r[14];
            Boolean tituloMod = (Boolean) r[15];
            Boolean proximaRevisionMod = (Boolean) r[16];
            Boolean versionMod = (Boolean) r[17];
            Boolean departamentoMod = (Boolean) r[18];
            Boolean documentoMod = (Boolean) r[19];
            Boolean notasMod = (Boolean) r[20];
            Boolean enabledMod = r[21] == null ? false : (Boolean) r[21]; //Falso por default
            Boolean fileNameMod = (Boolean) r[22];

            String usuario = (String) r[23];
            Date revisionDate = Date.from(Instant.ofEpochMilli(((BigInteger) r[24]).longValue()));
            Boolean documentoEditableMod = (Boolean) r[25];

            // Ejemplo de la descripción de la revisión: "Juan modificó el código universal a 1009, cliente a Tamto, descripción a 'Nueva Descripción' hace 3 días"
            StringBuilder descripcionRev = new StringBuilder();
            descripcionRev.append("modificó ");

            Boolean coma = false;
            if(nivelMod != null && nivelMod) {
                if(coma) { descripcionRev.append(", "); }
                descripcionRev.append("el nivel a \""+ nivel +"\"");
                coma = true;
            }
            if(tipoMod != null && tipoMod) {
                if(coma) { descripcionRev.append(", "); }
                descripcionRev.append("tipo de documento por \""+tipo+"\"");
                coma = true;
            }
            if(codigoMod != null && codigoMod) {
                if(coma) { descripcionRev.append(", "); }
                descripcionRev.append("el código a \""+codigo+"\"");
                coma = true;
            }
            if(rolcsMod != null && rolcsMod) {
                if(coma) { descripcionRev.append(", "); }
                descripcionRev.append("el ROLCS por \""+rolcs+"\"");
                coma = true;
            }
            if(tituloMod != null && tituloMod) {
                if(coma) { descripcionRev.append(", "); }
                descripcionRev.append("título a \""+titulo+"\"");
                coma = true;
            }
            if(proximaRevisionMod != null && proximaRevisionMod) {
                if(coma) { descripcionRev.append(", "); }
                descripcionRev.append("fecha de próxima revisión por \""+proximaRevision+"\"");
                coma = true;
            }
            if(versionMod != null && versionMod) {
                if(coma) { descripcionRev.append(", "); }
                descripcionRev.append("la versión a \""+version+"\"");
                coma = true;
            }
            if(departamentoMod != null && departamentoMod) {
                if(coma) { descripcionRev.append(", "); }
                descripcionRev.append("departamento a \""+departamento+"\"");
                coma = true;
            }
            if(documentoMod != null && documentoMod) {
                if(coma) { descripcionRev.append(", "); }
                descripcionRev.append("el archivo PDF del documento");
                coma = true;
            }
            if(documentoEditableMod != null && documentoEditableMod) {
                if(coma) { descripcionRev.append(", "); }
                descripcionRev.append("documento fuente");
                coma = true;
            }
            if(notasMod != null && notasMod) {
                if(coma) { descripcionRev.append(", "); }
                descripcionRev.append("modificó las notas");
                coma = true;
            }
            if(enabledMod != null && enabledMod) {
                if(coma) { descripcionRev.append(", "); }
                descripcionRev.append(enabledMod ? "Recuperó el documento previamente archivado" : "Archivó el documento");
                coma = true;
            }
            if(fileNameMod != null && fileNameMod) {
                if(coma) { descripcionRev.append(", "); }
                descripcionRev.append("el nombre del archivo PDF a \""+fileName+"\"");
                coma = true;
            }
            /* Modificó otro atributo como fileType, fileSize, updated, created
            if(!coma) {
                descripcionRev.append("un atributo interno");
            }*/

            ItemAuditoria item = new ItemAuditoria(usuario, descripcionRev.toString(), Util.getElapsedTimeString(revisionDate));

            resultados.add( item );
        }

        DateFormat df = SimpleDateFormat.getDateInstance();
        resultados.add( new ItemAuditoria(autor, "registró por primera vez este documento", df.format(created)));

        return resultados;
    }

}
