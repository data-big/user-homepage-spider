package info.bbd.user.weibos.spider.service.utils;

public class StringUtils {
 
	/** 
     * 去除HTML字串中的控制字符及不可视字符 
     *  
     * @param str 
     *            HTML字串 
     * @return 返回的字串 
     */  
    public static String escapeHTML(String str) {  
        int length = str.length();  
        int newLength = length;  
        boolean someCharacterEscaped = false;  
        for (int i = 0; i < length; i++) {  
            char c = str.charAt(i);  
            int cint = 0xffff & c;  
            if (cint < 32)  
                switch (c) {  
                case 11:  
                default:  
                    newLength--;  
                    someCharacterEscaped = true;  
                    break;  
  
                case '\t':  
                case '\n':  
                case '\f':  
                case '\r':  
                    break;  
                }  
            else  
                switch (c) {  
                case '"':  
                    newLength += 5;  
                    someCharacterEscaped = true;  
                    break;  
  
                case '&':  
                case '\'':  
                    newLength += 4;  
                    someCharacterEscaped = true;  
                    break;  
  
                case '<':  
                case '>':  
                    newLength += 3;  
                    someCharacterEscaped = true;  
                    break;  
                }  
        }  
        if (!someCharacterEscaped)  
            return str;  
  
        StringBuffer sb = new StringBuffer(newLength);  
        for (int i = 0; i < length; i++) {  
            char c = str.charAt(i);  
            int cint = 0xffff & c;  
            if (cint < 32)  
                switch (c) {  
                case '\t':  
                case '\n':  
                case '\f':  
                case '\r':  
                    sb.append(c);  
                    break;  
                }  
            else  
                switch (c) {  
                case '"':  
                    sb.append("&quot;");  
                    break;  
  
                case '\'':  
                    sb.append("&apos;");  
                    break;  
  
                case '&':  
                    sb.append("&amp;");  
                    break;  
  
                case '<':  
                    sb.append("&lt;");  
                    break;  
  
                case '>':  
                    sb.append("&gt;");  
                    break;  
  
                default:  
                    sb.append(c);  
                    break;  
                }  
        }  
        return sb.toString();  
    }
    
    public static String deleteAllCRLF(String input) {  
        return input.replaceAll("((\r\n)|\n)[\\s\t ]*", "").replaceAll(  
                "^((\r\n)|\n)", "");  
    }  
}