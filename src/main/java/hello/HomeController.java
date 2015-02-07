package hello;

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
import org.springframework.web.servlet.ModelAndView;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
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
    EntityManager em;

    @RequestMapping("/")
    public String dashboard(Model model) {

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

        return "home";
    }

    @RequestMapping("/403")
    public String prohibido(Model model) {
        model.addAttribute("selectedMenu", "dashboard");
        return "403";
    }

    @RequestMapping("/404")
    public String noEncontrado(Model model) {
        model.addAttribute("selectedMenu", "dashboard");
        return "404";
    }

    private static final String PATH = "/error";

    @RequestMapping(PATH)
    @ExceptionHandler(value = Exception.class)
    public ModelAndView handleError(HttpServletRequest request, Exception e)
    {
       ModelAndView mav = new ModelAndView("/500headless");
       mav.addObject("exception", e);
       mav.addObject("url", request.getRequestURL());
       mav.addObject("type", e.getClass().toGenericString());
       mav.addObject("error", e.getMessage());
       mav.addObject("selectedMenu", "dashboard");
       return mav;
    }

    @Override
    public String getErrorPath() {
        return PATH;
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
        for(User user : userRepository.findActiveUsers()) {
            usuarios.put(user.getUsername(), user);
        }
        return usuarios;
    }

    private List<Rev> createRevisions() {
        AuditReader reader = AuditReaderFactory.get(em);
        Class[] clases = new Class[] {Pieza.class, User.class, Archivo.class};
        TreeMap<Long, Rev> map = new TreeMap<>(Comparator.<Long>reverseOrder());
        HashMap<String, User> usuarios = getUsuarios();

        for (Class clase : clases) {
            AuditQuery query = reader.createQuery()
            .forRevisionsOfEntity(clase, false, true)
            .addOrder(AuditEntity.revisionProperty("timestamp").desc())
            .setMaxResults(5);

            List<Object[]> results = query.getResultList();

            for(Object[] o : results) {
                Revision track = (Revision) o[1];
                RevisionType changeType = (RevisionType) o[2];
                Class entityClass = o[0].getClass();
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
            return "Sin descripción";
        }
    }
}
