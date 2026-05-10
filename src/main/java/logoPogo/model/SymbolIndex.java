package logoPogo.model;

import java.util.HashMap;
import java.util.Map;

public class SymbolIndex {

    public final Map<String, Ast.Procedure> procedures = new HashMap<>();

    public static SymbolIndex from(Ast.Program p){
        SymbolIndex index = new SymbolIndex();
        for(Ast.Procedure proc : p.Procedure){
            if (!proc.name.isEmpty()) {
                index.procedures.putIfAbsent(proc.name,proc);
            }
        }
        return index;
    }

}
