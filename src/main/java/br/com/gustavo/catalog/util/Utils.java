package br.com.gustavo.catalog.util;

import br.com.gustavo.catalog.entities.Product;
import br.com.gustavo.catalog.projections.IdProjection;
import br.com.gustavo.catalog.projections.ProductProjection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {

    // ele pega a ordenação de ProductProjection(page) e monta uma nova ordem de produtos ordenada na nova lista (entities)
    public static <ID> List<? extends IdProjection<ID>> replace(List<? extends IdProjection<ID>> ordered, List<? extends IdProjection<ID>> unordered) {

        Map<ID, IdProjection<ID>> map = new HashMap<>();
        for (IdProjection<ID> obj : unordered) {
            map.put(obj.getId(), obj);
        }

        List<IdProjection<ID>> result = new ArrayList<>();
        for (IdProjection<ID> obj : ordered) {
            result.add(map.get(obj.getId()));
        }

        return result;
    }
}
