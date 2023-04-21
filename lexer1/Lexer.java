/** 词法分析器要求
 *  关键字: int, if, for
 *     标识符: 字母(字母|数字)*
 *     无符号数：包括科学计数法，浮点数，整数
 *     运算符或分界符：=, *, +, ++, +=, {, },
 * @author 杨光
 * @date 2023/04/19
 */
package lexer1;

public class Lexer {
    private String lexeme = ""; //存放具有独立意义的单词
    private int attributes; //对应符号表的编号
    private int currentIndex = 0; //对应当前识别的指针的位置

    public boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    public boolean isLetter(char c) {
        return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z';
    }

    /** 得到一个字符串中的独立的数字部分，遇到不是字符的就停止 */
    public String getDependentSumOfNumber(String s) {
        char peek;
        String lexeme = "";
        int sumOfNumber = 0;
        while (currentIndex < s.length()) {
            peek = s.charAt(currentIndex);
            if (isDigit(peek)) {
                sumOfNumber = sumOfNumber * 10 + (peek - '0');
                currentIndex++;
            } else {
                break;
            }
        }
        lexeme += sumOfNumber;
        return lexeme;
    }

    /** 得到字符串中完整的数字串包括浮点数的小数点和科学计数法 */
    public String getNumber(String s) {
        String lexeme = "";
        char peek;
        /* 查看字符串中的独立的数字 */
        lexeme += getDependentSumOfNumber(s);

        /* 查看下一个字符是否是'.'或者是'e''E'，如果是继续获得后续的独立数字*/
        while (currentIndex < s.length()) {
            peek = s.charAt(currentIndex);
            if (peek == '.') {
                lexeme += peek;
                currentIndex++;
                lexeme += getDependentSumOfNumber(s);
            } else if (peek == 'e' || peek == 'E') {
                lexeme += peek;
                currentIndex++;
                lexeme += getDependentSumOfNumber(s);
                /* 正负号只能出现在e或E的后面，检查lexeme的最后以为是否为e或E. */
            } else if ((lexeme.charAt(lexeme.length() - 1) == 'e' || lexeme.charAt(lexeme.length() - 1) == 'E')
                    && (peek == '+' || peek == '-')) {
                lexeme += peek;
                currentIndex++;
                lexeme += getDependentSumOfNumber(s);
            } else if (isDigit(peek)) {
                /* 继续查看剩下字符串中是否还有独立的数字 */
                lexeme += getDependentSumOfNumber(s);
            } else if (peek == ' ' || peek == '\t' || peek == '\n') {
                currentIndex++;
                break;
            } else { /* 如果出现其它字符直接退出循环. */
                break;
            }
        }
        attributes = 20;
        return lexeme;
    }

    /* 得到字符串中具有独立意义的单词例如关键字int，for，if;标识符;运算符或者分界符.*/
    public String getString(String s) {
        char[] content = s.toCharArray();
        char peek;
        String lexeme = "";
        while (currentIndex < content.length) {
            peek = content[currentIndex];
            if (isLetter(peek)) {
                if (lexeme.contains("+")) {
                    return lexeme;
                }
                lexeme += peek;
                currentIndex++;
                attributes = 21;
                switch (lexeme) {
                    case "int" -> {
                        attributes = 10;
                        return lexeme;
                    }
                    case "if" -> {
                        attributes = 11;
                        return lexeme;
                    }
                    case "for" -> {
                        attributes = 12;
                        return lexeme;
                    }
                }
                /* 最后一个条件是要满足lexeme的内容不为"+"，且此时的peek为"=";如果此时lexeme内容为"+",
                   peek内容为"="不进入这个语句，进入下一个else，判断是否为++或者+=.
                 */
            } else if(peek == '{' || peek == '}' || peek == '*' || (!lexeme.equals("+") && peek == '=')) {
                if (!lexeme.equals("")) {
                    return lexeme;
                }
                lexeme = "" + peek;
                currentIndex++;
                switch (peek) {
                    case '{' -> attributes = 18;
                    case '}' -> attributes = 19;
                    case '*' -> attributes = 14;
                    case '=' -> attributes = 13;
                }
                break;
                /* 识别单独的"+"，确认lexeme中没有前导的"+" */
            } else if(!lexeme.equals("+") && peek == '+') {
                if (!lexeme.equals("")) {
                    return lexeme;
                }
                lexeme += "" + peek;
                currentIndex++;
                attributes = 15;
                /* 识别"++" 或 "+=" */
            } else if(lexeme.equals("+") && (peek == '+' || peek == '=')) {
                lexeme += peek;
                currentIndex++;
                if (peek == '+') {
                    attributes = 16;
                }else if (peek == '=') {
                    attributes = 17;
                }
                break;
                /* 识别空白符 */
            } else if (peek == ' ' || peek == '\t' || peek == '\n') {
                /* 如果lexeme不为空，然后识别到了空白符，需要回退，返回lexeme，下一次再识别空白符.*/
                if (!lexeme.equals("")) {
                    return lexeme;
                }
                currentIndex++;
                attributes = 22;
                lexeme = " ";
                break;
            } else if (isDigit(peek)) {
                lexeme += getNumber(s);
                attributes = 21;
            }
        }
        return lexeme;
    }

    /* 词法分析的核心 */
    public void scan(String s) {
        char[] content = s.toCharArray();
        System.out.println(content.length);
        char peek;
        while (currentIndex < content.length) {
            lexeme = "";
            peek = content[currentIndex];
            if (isDigit(peek)) {
                lexeme = getNumber(s);
                showStatus();
            } else {
                lexeme = getString(s);
                showStatus();
            }
        }
        System.out.println("Lexer analysis complete!\n");
        currentIndex = 0;
    }

    public void showStatus() {
        System.out.println("[" + Table.map.get(attributes)
                + ":" + attributes + "]" + lexeme);
    }
}
