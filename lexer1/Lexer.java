package lexer1;

public class Lexer {
    public String lexeme = "";
    public int attributes;
    public int currentIndex = 0;
    public int status;

    public boolean isDigit(char c) {
        if (c >= '0' && c <= '9') {
            return true;
        }
        return false;
    }

    public boolean isLetter(char c) {
        if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z') {
            return true;
        }
        return false;
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
        char peek ;
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
            } else if (peek == '+' || peek == '-') {
                lexeme += peek;
                currentIndex++;
                lexeme += getDependentSumOfNumber(s);
            } else if (isDigit(peek)) {
                /* 继续查看剩下字符串中是否还有独立的数字 */
                lexeme += getDependentSumOfNumber(s);
            } else if (peek == ' ' || peek == '\t' || peek == '\n') {
                currentIndex++;
            }
        }
        return lexeme;
    }

    public String getString(String s) {
        //TODO
        return null;
    }

    public int scan(String s) {
        char[] content = s.toCharArray();
        System.out.println(content.length);
        lexeme = "";
        char peek = content[currentIndex];
        if (isDigit(peek)) {
            lexeme = getNumber(s);

            attributes = 20;

        } else if(isLetter(peek)) {
            //TODO
        }
        return attributes;
    }

    public void showStatus(int attributes) {
        System.out.println("[" + Table.map.get(attributes)
                + ":" + attributes + "]" + lexeme);
    }
}
