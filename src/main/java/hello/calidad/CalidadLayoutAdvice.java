package hello.calidad;

import hello.Propiedad;
import hello.PropiedadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Proyecto tamto
 * User: octavioruizcastillo
 * Date: 02/05/15
 * Time: 16:17
 */
@ControllerAdvice("hello.calidad")
public class CalidadLayoutAdvice {

    @Autowired
    PropiedadRepository propiedadRepository;

    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("selectedMenu", "calidad");
        model.addAttribute("encabezado", getEncabezado().getVal());
    }

    private Propiedad getEncabezado() {
        //ENCABEZADO-CALIDAD es una propiedad constante alojada en la tabla propiedades de la BD,
        //  su función es almacenar el HTML que se mostrará como encabezado en toda la sección de calidad
        Propiedad encabezado = propiedadRepository.findOne("ENCABEZADO-CALIDAD");

        //Null-safe, En caso de que la propiedad no sea encontrada se enviará el placeholder
        if(encabezado == null) {
            encabezado = new Propiedad();
            encabezado.setKey("ENCABEZADO-CALIDAD");
            encabezado.setVal("<h1>Sin encabezado</h1><p>Por favor edita mi contenido, pasando el cursor por aquí y oprimiendo 'Editar encabezado'.</p>");
        }
        return encabezado;
    }
}
