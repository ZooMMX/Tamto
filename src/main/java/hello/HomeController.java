package hello;

import hello.calidad.DocumentoInterno;
import hello.calidad.ListaMaestraRepository;
import hello.util.CloudConvertConnector;
import org.hibernate.envers.*;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.ocpsoft.prettytime.PrettyTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

/**
 * Proyecto Omoikane: SmartPOS 2.0
 * User: octavioruizcastillo
 * Date: 08/12/14
 * Time: 11:29
 */
@Controller
public class HomeController implements ErrorController {
    @Autowired
    UserRepository userRepository;

    @Autowired
    PiezaRepository piezaRepository;

    @Autowired
    ArchivoRepository archivoRepository;

    @Autowired
    ListaMaestraRepository listaMaestraRepository;

    @Autowired
    EntityManager em;

    @RequestMapping("/")
    public String dashboard(Model model) throws ClassNotFoundException {

        List<DocumentoInterno> calidadProximasRevisiones = listaMaestraRepository.getDocumentosProximaRevision();
        List<DocumentoInterno> calidadRevisionesVencidas = listaMaestraRepository.getDocumentosRevisionVencida();

        List usuarios = (List) userRepository.findActiveUsers();
        Long piezaSize = piezaRepository.count();
        Long archivoSize = archivoRepository.count();

        /* Definir datos de la gráfica */
        String graphData = createGraphData();

        /* Definir datos de las notificaciones */
        List<Rev> revisiones = createRevisions();

        model.addAttribute("selectedMenu", "dashboard");
        model.addAttribute("noUsuarios", usuarios.size());
        model.addAttribute("noPiezas", piezaSize);
        model.addAttribute("noArchivos", archivoSize);
        model.addAttribute("graphData", graphData);
        model.addAttribute("revisiones", revisiones);
        model.addAttribute("calidadProximasRevisiones", calidadProximasRevisiones);
        model.addAttribute("calidadRevisionesVencidas", calidadRevisionesVencidas);

        return "home";
    }

    @RequestMapping("/info")
    public @ResponseBody
    Object info(Model model) {
        Query q = em.createNativeQuery("show variables like '%max_allowed_packet%';");
        Object result = q.getSingleResult();
        return result;
    }

    @RequestMapping("/updateFilePreviews")
    public @ResponseBody
    Object updateFilePreviews(Model model) {
        List<Archivo> procesados = new ArrayList<Archivo>();
        List<Archivo> fallidos = new ArrayList<Archivo>();
        Map<String, List<Archivo>> map = new HashMap<>();
        map.put("Procesados", procesados);
        map.put("Fallidos", fallidos);

        List<Long> ids = (List<Long>) archivoRepository.findAllIds();
        CloudConvertConnector connector = new CloudConvertConnector();
        // Para cada Archivo en la base de datos
        for(Long id : ids) {
            //Consulto el archivo, uno por uno para no llenar la memoria
            Archivo a = archivoRepository.findOne(id);
            System.out.println("Procesando: " + a.getFileName());
            try {
                //Intento la conversión
                connector.addPdfPreview(a);
                //Intento actualizarlo
                archivoRepository.save(a);

                //Lo registro como exitoso
                procesados.add(a);
            } catch (Exception e) {
                e.printStackTrace();
                //Lo registro como fallido
                fallidos.add(a);
            }
        }


        return map;
    }

    @RequestMapping("/manual")
    public String manual(Model model) {
        model.addAttribute("selectedMenu", "manual");
        return "manual";
    }

    // Error page
    @RequestMapping("/error")
    public String error(HttpServletRequest request, Model model) {

        Integer errorCode;
        String errorMsg;
        errorCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        errorMsg  = (String)  request.getAttribute("javax.servlet.error.message");
        model.addAttribute("errorCode", errorCode);
        model.addAttribute("selectedMenu", "");
        errorCode = errorCode==null ? 500 :errorCode;
        errorMsg  = errorMsg ==null ? "Error interno" : errorMsg;

        if(errorCode == 404) {
            return "404";
        } else if(errorCode == 403) {
            return "403";
        } else {
            Throwable throwable = (Throwable) request.getAttribute("org.springframework.web.servlet.DispatcherServlet.EXCEPTION");
            //throw safe
            if (throwable == null) {
                throwable = (Throwable) request.getAttribute("javax.servlet.error.exception");
                if(throwable == null) {
                    throwable = new Throwable("Error "+errorCode+". "+errorMsg);
                }
            }
            if(throwable != null) {
                throwable.printStackTrace();
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                throwable.printStackTrace(pw);
                model.addAttribute("fullStacktrace", sw.toString().split("\n"));
                model.addAttribute("url", request.getRequestURL());
            }
                model.addAttribute("selectedMenu", "dashboard");
            model.addAttribute("throwable", throwable);
            return "error";
        }
    }

    @Override
    public String getErrorPath() {
        return "/error";
    }

    @RequestMapping("/403")
    public String forbidden(HttpServletRequest request, Model model) {
        model.addAttribute("selectedMenu", "dashboard");
        return "403";
    }

    /**
     * Crea un String que contiene un arreglo de objetos tipo hash en lenguaje javascript. P.e.: [{"key":"val"},{"key2":"val2"}]
     * @return
     */
    private String createGraphData() {
        StringBuilder sb = new StringBuilder();
        List<Object[]> conteoPorTipo = piezaRepository.countByType();
        sb.append("[");
        for(int i = 0; i < conteoPorTipo.size(); i++) {
            if(i>0) sb.append(",");
            Object[] c = conteoPorTipo.get(i);
            sb.append( "{label:\"" + c[0] + "\",data:\"" + c[1] + "\"}"); //Formato {"label":"valor","data":"valor"}
        }
        sb.append("]");

        return sb.toString();
    }

    private HashMap<String, User> getUsuarios() {
        HashMap<String, User> usuarios = new HashMap<String, User>();
        //Obtiene todos los usuarios para evitar un error en dashboard cuando un ususario es desactivado.
        for(User user : userRepository.findAll()) {
            usuarios.put(user.getUsername(), user);
        }
        return usuarios;
    }

    private List<Rev> createRevisions() throws ClassNotFoundException {
        AuditReader reader = AuditReaderFactory.get(em);
        Class[] clases = new Class[] {Pieza.class, User.class, Archivo.class, DocumentoInterno.class};
        TreeMap<Long, Rev> map = new TreeMap<>(Comparator.<Long>reverseOrder());
        HashMap<String, User> usuarios = getUsuarios();

        Query qUser    = em.createNativeQuery("SELECT rv.entityname, r.id, r.username, ua.revtype, r.timestamp FROM revision r JOIN revchanges rv ON rv.rev = r.id JOIN user_aud ua ON ua.rev = rv.rev WHERE rv.entityname = \"hello.User\" ORDER BY r.timestamp DESC LIMIT 5;");
        Query qPieza   = em.createNativeQuery("SELECT rv.entityname, r.id, r.username, pa.revtype, r.timestamp FROM revision r JOIN revchanges rv ON rv.rev = r.id JOIN pieza_aud pa ON pa.rev = rv.rev WHERE rv.entityname = \"hello.Pieza\" ORDER BY r.timestamp DESC LIMIT 5;");
        Query qArchivo = em.createNativeQuery("SELECT rv.entityname, r.id, r.username, aa.revtype, r.timestamp FROM revision r JOIN revchanges rv ON rv.rev = r.id JOIN archivo_aud aa ON aa.rev = rv.rev WHERE rv.entityname = \"hello.Archivo\" ORDER BY r.timestamp DESC LIMIT 5;");
        Query qDoc     = em.createNativeQuery("SELECT rv.entityname, r.id, r.username, da.revtype, r.timestamp FROM revision r JOIN revchanges rv ON rv.rev = r.id JOIN documento_interno_aud da ON da.rev = rv.rev WHERE rv.entityname = \"hello.calidad.DocumentoInterno\" ORDER BY r.timestamp DESC LIMIT 5;");
        List<List<Object[]>> cambios = Arrays.asList( qUser.getResultList(), qPieza.getResultList(), qArchivo.getResultList(), qDoc.getResultList() );

        for (List<Object[]> results : cambios) {

            for(Object[] o : results) {
                Revision track = new Revision(); // Revision) o[1];
                track.setUsername((String) o[2]);
                track.setId((Integer) o[1]);
                track.setTimestamp(((BigInteger)o[4]).longValue());
                RevisionType changeType = RevisionType.fromRepresentation(o[3]);
                Class entityClass = Class.forName(String.valueOf(o[0]));
                Rev rev = new Rev(entityClass, track, changeType, usuarios);
                map.put(track.getTimestamp(), rev);
            }
        }

        ArrayList<Rev> revisiones = new ArrayList<Rev>();
        for (Rev rev : map.values()) {
            revisiones.add(rev);
        }
        return revisiones;
    }

    class Rev {

        private RevisionType changeType;
        private Revision revision;
        private Class entityClass;
        private HashMap<String, User> usuarios;

        public Rev(Class entityClass, Revision revision, RevisionType changeType, HashMap<String, User> usuarios) {
            this.changeType = changeType;
            this.revision = revision;
            this.entityClass = entityClass;
            this.usuarios = usuarios;
        }

        public RevisionType getChangeType() {
            return changeType;
        }

        public void setChangeType(RevisionType changeType) {
            this.changeType = changeType;
        }

        public Date getDate() {
            return revision.getRevisionDate();
        }

        public String getUserName() { return revision.getUsername(); }

        public String getUserFullName() { return usuarios.get(revision.getUsername()).getFullname(); }

        public Class getEntityClass() {
            return entityClass;
        }

        public void setEntityClass(Class entityClass) {
            this.entityClass = entityClass;
        }

        public String getElapsedTimeString() {
            PrettyTime p = new PrettyTime(new Locale("es"));
            return p.format(getDate());
        }

        @Override
        public String toString() {
            return getDescripcion()+" "+getElapsedTimeString()+" por "+getUserFullName();
        }

        public String getIconHtml() {
            if(getEntityClass() == Pieza.class) {
                switch(getChangeType()) {
                    case ADD:
                        return "<div class=\"label label-sm label-success\"><i class=\"icon-puzzle\"></i></div>";
                    case DEL:
                        return "<div class=\"label label-sm label-danger\"><i class=\"icon-puzzle\"></i></div>";
                    case MOD:
                        return "<div class=\"label label-sm label-default\"><i class=\"icon-puzzle\"></i></div>";
                }
            }
            else if(getEntityClass() == Archivo.class) {
                switch(getChangeType()) {
                    case ADD:
                        return "<div class=\"label label-sm label-success\"><i class=\"fa fa-briefcase\"></i></div>";
                    case DEL:
                        return "<div class=\"label label-sm label-danger\"><i class=\"fa fa-briefcase\"></i></div>";
                    case MOD:
                        return "<div class=\"label label-sm label-default\"><i class=\"fa fa-briefcase\"></i></div>";
                }
            }
            else if(getEntityClass() == User.class) {
                switch(getChangeType()) {
                    case ADD:
                        return "<div class=\"label label-sm label-success\"><i class=\"fa fa-user\"></i></div>";
                    case DEL:
                        return "<div class=\"label label-sm label-danger\"><i class=\"fa fa-user\"></i></div>";
                    case MOD:
                        return "<div class=\"label label-sm label-default\"><i class=\"fa fa-user\"></i></div>";
                }
            }
            else if(getEntityClass() == DocumentoInterno.class) {
                switch(getChangeType()) {
                    case ADD:
                        return "<div class=\"label label-sm label-success\"><i class=\"icon-check\"></i></div>";
                    case DEL:
                        return "<div class=\"label label-sm label-danger\"><i class=\"icon-check\"></i></div>";
                    case MOD:
                        return "<div class=\"label label-sm label-default\"><i class=\"icon-check\"></i></div>";
                }
            }
            return "";
        }

        public String getDescripcion() {
            if(getEntityClass() == Pieza.class) {
                switch(getChangeType()) {
                    case ADD:
                        return "Pieza agregada";
                    case DEL:
                        return "Pieza eliminada";
                    case MOD:
                        return "Pieza modificada";
                }
            }
            else if(getEntityClass() == Archivo.class) {
                switch(getChangeType()) {
                    case ADD:
                        return "Archivo agregado";
                    case DEL:
                        return "Archivo eliminado";
                    case MOD:
                        return "Archivo modificado";
                }
            }
            else if(getEntityClass() == User.class) {
                switch(getChangeType()) {
                    case ADD:
                        return "Usuario agregado";
                    case DEL:
                        return "Usuario eliminado";
                    case MOD:
                        return "Usuario modificado";
                }
            }
            else if(getEntityClass() == DocumentoInterno.class) {
                switch(getChangeType()) {
                    case ADD:
                        return "Documento de calidad agregado";
                    case DEL:
                        return "Documento de calidad eliminado";
                    case MOD:
                        return "Documento de calidad modificado";
                }
            }
            return "Sin descripción";
        }
    }
}
