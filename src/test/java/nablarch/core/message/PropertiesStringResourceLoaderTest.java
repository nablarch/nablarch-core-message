package nablarch.core.message;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.CharBuffer;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import nablarch.core.util.FileUtil;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import mockit.Capturing;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mock;
import mockit.Mocked;

/**
 * {@link PropertiesStringResourceLoader}のテストクラス。
 */
public class PropertiesStringResourceLoaderTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    /** テスト対象クラス */
    private PropertiesStringResourceLoader sut;

    @Before
    public void setUp() throws Exception {
        sut = new PropertiesStringResourceLoader();
    }

    /**
     * {@link PropertiesStringResourceLoader#getValue(Object)}のテスト。
     * デフォルト設定でプロパティファイルをロードするケース。
     *
     * @throws Exception
     */
    @Test
    public void testGetValue_default() throws Exception {
        StringResource result = sut.getValue("default.key");

        assertThat(result.getId(), is("default.key"));
        assertThat(result.getValue(Locale.getDefault()), is("デフォルト"));
    }

    /**
     * {@link PropertiesStringResourceLoader#getValue(Object)}のテスト。
     * 設定をカスタマイズしてプロパティファイルをロードするケース。
     *
     * @throws Exception
     */
    @Test
    public void testGetValue_custom() throws Exception {
        sut.setDirectory("classpath:nablarch/core/message/");
        sut.setFileName("custom");
        sut.setDefaultLocale("en");
        StringResource result = sut.getValue("custom.key");

        assertThat(result.getId(), is("custom.key"));
        assertThat(result.getValue(Locale.ENGLISH), is("customValue"));
    }

    /**
     * {@link PropertiesStringResourceLoader#getValue(Object)}のテスト。
     * ロケール一覧を設定してプロパティファイルをロードするケース。
     *
     * @throws Exception
     */
    @Test
    public void testGetValue_locale() throws Exception {
        List<String> locales = new ArrayList<String>();
        locales.add("en");
        locales.add("zh");
        locales.add("de");
        sut.setLocales(locales);
        StringResource result = sut.getValue("locale.key");

        assertThat(result.getId(), is("locale.key"));
        assertThat(result.getValue(Locale.ENGLISH), is("localeValue_en"));
        assertThat(result.getValue(Locale.CHINESE), is("localeValue_zh"));
        assertThat(result.getValue(Locale.GERMANY), is("localeValue_de"));
    }

    /**
     * {@link PropertiesStringResourceLoader#getValue(Object)}のテスト。
     * メッセージ一覧がすでにキャッシュされているケース。
     *
     * @throws Exception
     */
    @Test
    public void testGetValue_exist() throws Exception {
        Map<String, Map<String, String>> messages = Deencapsulation.getField(sut, "messages");
        Map<String, String> value = new HashMap<String, String>();
        value.put(Locale.getDefault()
                        .getLanguage(), "existedValue");
        messages.put("existed.key", value);
        StringResource result = sut.getValue("existed.key");

        assertThat(result.getId(), is("existed.key"));
        assertThat(result.getValue(Locale.getDefault()), is("existedValue"));
    }

    /**
     * {@link PropertiesStringResourceLoader#getValue(Object)}のテスト。
     * 存在しないキーを引数に指定するケース。
     *
     * @throws Exception
     */
    @Test
    public void testGetValue_invalid() throws Exception {
        StringResource result = sut.getValue("invalid.key");

        assertThat(result, is(nullValue()));
    }

    /**
     * {@link PropertiesStringResourceLoader#getValue(Object)}のテスト。
     * nullを引数に指定するケース。
     *
     * @throws Exception
     */
    @Test
    public void testGetValue_null() throws Exception {
        StringResource result = sut.getValue(null);

        assertThat(result, is(nullValue()));
    }

    /**
     * {@link PropertiesStringResourceLoader#loadAll()}のテスト。
     *
     * @throws Exception
     */
    @Test
    public void testLoadAll_default() throws Exception {
        List<StringResource> result = sut.loadAll();

        assertThat(result.size(), is(4));

        assertThat(result, containsInAnyOrder(
                hasProperty("id", is("default.key")),
                hasProperty("id", is("load.all.key")),
                hasProperty("id", is("message.with.placeholder")),
                hasProperty("id", is("message"))
        ));
    }

    /**
     * {@link PropertiesStringResourceLoader#loadAll()}のテスト。
     * メッセージ一覧がすでにキャッシュされているケース。
     *
     * @throws Exception
     */
    @Test
    public void testLoadAll_exist() throws Exception {

        Map<String, Map<String, String>> messages = Deencapsulation.getField(sut, "messages");
        Map<String, String> value1 = new HashMap<String, String>();
        Map<String, String> value2 = new HashMap<String, String>();
        Map<String, String> value3 = new HashMap<String, String>();
        value1.put(Locale.getDefault()
                         .getLanguage(), "existed1Value");
        value2.put(Locale.getDefault()
                         .getLanguage(), "existed2Value");
        value3.put(Locale.getDefault()
                         .getLanguage(), "existed3Value");
        messages.put("existed1.key", value1);
        messages.put("existed2.key", value2);
        messages.put("existed3.key", value3);
        List<StringResource> result = sut.loadAll();

        assertThat(result.size(), is(3));
        for (StringResource sr : result) {
            if ("existed1.key".equals(sr.getId())) {
                assertThat(sr.getValue(Locale.getDefault()), is("existed1Value"));
            }
            if ("existed2.key".equals(sr.getId())) {
                assertThat(sr.getValue(Locale.getDefault()), is("existed2Value"));
            }
            if ("existed3.key".equals(sr.getId())) {
                assertThat(sr.getValue(Locale.getDefault()), is("existed3Value"));
            }
        }
    }

    /**
     * {@link PropertiesStringResourceLoader#getValues(String, Object)}のテスト。
     *
     * @throws Exception
     */
    @Test
    public void testGetValues() throws Exception {
        assertThat(sut.getValues("indexName", "key"), is(nullValue()));
    }

    /**
     * {@link PropertiesStringResourceLoader#getId(StringResource)}のテスト。
     *
     * @throws Exception
     */
    @Test
    public void testGetId() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        map.put(Locale.getDefault()
                      .getLanguage(), "testValue");
        assertThat(sut.getId(new BasicStringResource("test.key", map)), is((Object) "test.key"));
    }

    /**
     * {@link PropertiesStringResourceLoader#generateIndexKey(String, StringResource)}のテスト。
     *
     * @throws Exception
     */
    @Test
    public void testGenerateIndexKey() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        StringResource result = new BasicStringResource("test.key", map);
        assertThat(sut.generateIndexKey("indexName", result), is(nullValue()));
    }

    /**
     * {@link PropertiesStringResourceLoader#getIndexNames()}のテスト。
     *
     * @throws Exception
     */
    @Test
    public void testGetIndexNames() throws Exception {
        assertThat(sut.getIndexNames(), is(nullValue()));
    }
}