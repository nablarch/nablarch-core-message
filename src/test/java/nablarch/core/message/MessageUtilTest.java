package nablarch.core.message;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.util.Locale;

import nablarch.core.ThreadContext;
import nablarch.core.repository.SystemRepository;
import nablarch.test.support.SystemRepositoryResource;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class MessageUtilTest {

    @Rule
    public SystemRepositoryResource repositoryResource = new SystemRepositoryResource(
            "nablarch/core/message/message-resource.xml");
    
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        Locale.setDefault(Locale.JAPAN);
    }

    @Test
    public void testGetMessageObject() throws Exception {

        StringResource messageObject = MessageUtil.getStringResource("default.key");
        assertThat(messageObject.getId(), is("default.key"));
        assertThat(messageObject.getValue(Locale.JAPANESE), is("デフォルト"));
        assertThat(messageObject.getValue(Locale.ENGLISH), is("default key"));
    }

    @Test
    public void testCreateResultMessage() throws Exception {

        Message message = MessageUtil.createMessage(MessageLevel.INFO, "message.with.placeholder",
                MessageUtil.createMessage(MessageLevel.INFO, "message"), "test2");

        assertThat(message.getLevel(), is(MessageLevel.INFO));
        assertThat(message.getMessageId(), is("message.with.placeholder"));
        assertThat(message.formatMessage(Locale.JAPANESE), is("ここにmessageのメッセージが入る→埋め込みメッセージ-test2"));
    }

    /**
     * スレッドコンテキストに言語が設定されていない場合、
     * VMのデフォルトロケールの言語が使用されること。
     */
    @Test
    public void testDefaultLocale() throws Exception {

        Message message = MessageUtil.createMessage(
                MessageLevel.INFO,
                "message.with.placeholder",
                MessageUtil.createMessage(MessageLevel.INFO, "message"),
                "test2");

        ThreadContext.setLanguage(null);

        assertThat(message.getLevel(), is(MessageLevel.INFO));
        assertThat(message.getMessageId(), is("message.with.placeholder"));
        assertThat(message.formatMessage(), is("ここにmessageのメッセージが入る→埋め込みメッセージ-test2"));
    }

    @Test
    public void testDefaultProperties() throws Exception {
        SystemRepository.clear();

        Message message = MessageUtil.createMessage(MessageLevel.INFO, "default.key");

        assertThat(message.getLevel(), is(MessageLevel.INFO));
        assertThat(message.getMessageId(), is("default.key"));
        assertThat(message.formatMessage(), is("デフォルト"));

        message = MessageUtil.createMessage(MessageLevel.INFO, "load.all.key");

        assertThat(message.getLevel(), is(MessageLevel.INFO));
        assertThat(message.getMessageId(), is("load.all.key"));
        assertThat(message.formatMessage(), is("loadAllValue"));
    }

    @Test
    public void messageNotFound() throws Exception {
        expectedException.expect(MessageNotFoundException.class);
        expectedException.expectMessage("message was not found. message id = notFound");
        MessageUtil.getStringResource("notFound");
    }
}
