package hello.productos;

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
            @RequestParam Integer length,
            @RequestParam Integer start,
            Model model
    ) {
        Pageable pageable = new PageRequest(start, length);
        Page<Product> page = repo.findAll(pageable);
        List<Product> productList = page.getContent();

        model.addAttribute("products", productList);

        return "products/list";
    }
}
