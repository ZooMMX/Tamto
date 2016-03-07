package hello.productos;

import hello.PageWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;

/**
 * Created by octavioruiz on 24/02/16.
 */
@Controller
public class ProductsController {
    @Autowired
    ProductRepository repo;

    @RequestMapping("/products")
    public String listProducts(
            @RequestParam(name = "page.size", defaultValue = "10") Integer size,
            @RequestParam(name = "page.page", defaultValue = "1") Integer pageNumber,
            Model model
    ) {

        Pageable pageable = new PageRequest(pageNumber-1, size);  //start from -1 to match page.page
        Page<Product> page = repo.findAll(pageable);

        PageWrapper<Product> pageWrapper = new PageWrapper<>(
                page, "/products");
        model.addAttribute("page", pageWrapper);


        return "products/list";
    }
}
