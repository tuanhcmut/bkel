package org.hcmut.bkelbot;

public class Utils {
    public static String makeString (String displ){
        String result = displ.replaceAll("\\<.*?\\>", "\n");
        String previousResult = "";
        while(!previousResult.equals(result)){
            previousResult = result;
            result = result.replaceAll("\n\n","\n");
        }
        return result.replaceAll("&nbsp;","").replaceAll("&amp;","&").replaceAll("&gt;",">").replaceAll("&lt;","<").replaceAll("&quot;","\"");
    }
}
