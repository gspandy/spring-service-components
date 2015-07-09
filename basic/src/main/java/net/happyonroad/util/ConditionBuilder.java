package net.happyonroad.util;

import org.springframework.util.StringUtils;

/**
 * <h1>Map Builder</h1>
 *
 * @author Jay Xiong
 */
public final class ConditionBuilder {
    /**
     * <h2>将输入的参数序列，转换为一个map</h2>
     *
     * 如果pairs的value为null，则忽略
     *
     * @param pairs 参数序列
     * @return 转换之后的map
     */
    public static String buildCondition(String abbr, Object... pairs) {
        if (pairs.length % 2 != 0)
            throw new IllegalArgumentException("The pairs number should be even, but I got "
                                               + StringUtils.arrayToDelimitedString(pairs, ","));
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < pairs.length; i += 2) {
            String key = pairs[i].toString().replaceAll("'","''");
            key = "`" + key + "`";
            if( !StringUtils.isEmpty(abbr)){
                key = abbr + "." + key;
            }
            Object value = pairs[i+1];
            if( value == null ){
                builder.append("(").append(key).append("IS NULL)") ;
            }else{
                //Escape SQL by replaceAll
                String strValue = value.toString().replaceAll("'", "''");
                builder.append("(").append(key).append(" = '").append(strValue).append("'").append(")") ;
            }
            if(i+1 < pairs.length - 1){
                builder.append(" AND ");
            }
        }
        return builder.toString();
    }
}
