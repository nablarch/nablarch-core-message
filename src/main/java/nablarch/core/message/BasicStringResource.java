package nablarch.core.message;

import java.util.Locale;
import java.util.Map;

/**
 * StringResourceの基本実装クラス。
 * 
 * @author Koichi Asano
 *
 */
public class BasicStringResource implements StringResource {

    /**
     * メッセージID。
     */
    private String id;
    /**
     * 言語をキーとした文字列のmap。
     */
    private Map<String, String> formatMap;

    /**
     * コンストラクタ。
     * 
     * @param id メッセージID
     * @param formatMap 言語をキーとした文字列のmap
     */
    public BasicStringResource(String id, Map<String, String> formatMap) {
        this.id = id;
        this.formatMap = formatMap;
    }

    /**
     * {@inheritDoc}
     */
    public String getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    public String getValue(Locale locale) {
        if (!formatMap.containsKey(locale.getLanguage())) {
            throw new IllegalArgumentException("Lang was not supported. " 
                    + " message id = " + id
                    + " language = " + locale.getLanguage());
        }
        return formatMap.get(locale.getLanguage());
    }
}
