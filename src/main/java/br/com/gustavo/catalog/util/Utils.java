package br.com.gustavo.catalog.util;

import br.com.gustavo.catalog.entities.Product;
import br.com.gustavo.catalog.projections.ProductProjection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {

    // ele pega a ordenação de ProductProjection(page) e monta uma nova ordem de produtos ordenada na nova lista (entities)
    public static List<Product> replace(List<ProductProjection> ordered, List<Product> unordered) {

        Map<Long, Product> map = new HashMap<>();
        for (Product obj : unordered) {
            map.put(obj.getId(), obj);
        }

        List<Product> result = new ArrayList<>();
        for (ProductProjection obj : ordered) {
            result.add(map.get(obj.getId()));
        }

        return result;
    }
}
