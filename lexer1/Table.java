package lexer1;

import java.util.LinkedHashMap;
import java.util.Map;

public class Table {
    public static LinkedHashMap<Integer, String> map = new LinkedHashMap<>();

    static {
        map.put(10, "int");
        map.put(11, "if");
        map.put(12, "for");
        map.put(13, "=");
        map.put(14, "*");
        map.put(15, "+");
        map.put(16, "++");
        map.put(17, "+=");
        map.put(18, "{");
        map.put(19, "}");
        map.put(20, "NUM");
        map.put(21, "IDENTIFIER");
        map.put(22, "WS");
    }

    public static void showTable() {
        System.out.println("------符号表------");
        System.out.print("name:\tattributes:\n");
        for (Map.Entry<Integer, String> entry: map.entrySet()) {
            System.out.printf("%s\t\t%d\n", entry.getValue(), entry.getKey());
        }
        System.out.println("----------------");
    }
}
