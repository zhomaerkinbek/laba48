package laba;

import com.sun.net.httpserver.HttpExchange;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import server.BasicServer;
import server.ContentType;
import server.ResponseCodes;
import server.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Laba48 extends BasicServer {

    private final static Configuration freemarker = initFreeMarker();
    private static CandidatesModel candidates = new CandidatesModel();
    private static Candidate currentVotedCandidate = new Candidate();
    private static Integer allVotes = 0;

    public Laba48(String host, int port) throws IOException {
        super(host, port);
        registerGet("/", this::candidatesHandler);
        registerPost("/vote", this::voteHandler);
        registerGet("/thankyou", this::thanksHandler);
        registerGet("/votes", this::votesHandler);
    }

    private void votesHandler(HttpExchange exchange) {
        Map<String, Object> data = new HashMap<>();
        data.put("allvotes", allVotes);
        data.put("candidates", candidates.getCandidates().stream().sorted((Comparator.comparing(Candidate::getVotes)).reversed()).collect(Collectors.toList()));
        renderTemplate(exchange, "votes.html", data);
    }

    private void thanksHandler(HttpExchange exchange) {
        Map<String, Object> data = new HashMap<>();
        data.put("candidate", currentVotedCandidate);
        data.put("percent", (currentVotedCandidate.getVotes() * 100 / allVotes));
        renderTemplate(exchange, "thankyou.html", data);
    }

    private void voteHandler(HttpExchange exchange) {
        String raw = getBody(exchange);
        Map<String, String> parsed = Utils.parseUrlEncoded(raw, "&");
        for(var candidate: candidates.getCandidates()){
            if(parsed.get("candidateId").equalsIgnoreCase(candidate.getId().toString())){
                candidate.setVotes(candidate.getVotes() + 1);
                currentVotedCandidate = candidate;
                break;
            }
        }
        allVotes++;
        redirect303(exchange, "/thankyou");
    }

    private void candidatesHandler(HttpExchange exchange) {
        renderTemplate(exchange, "candidates.html", candidates);
    }


    private static Configuration initFreeMarker() {
        try {
            Configuration cfg = new Configuration(Configuration.VERSION_2_3_29);
            // путь к каталогу в котором у нас хранятся шаблоны
            // это может быть совершенно другой путь, чем тот, откуда сервер берёт файлы
            // которые отправляет пользователю
            cfg.setDirectoryForTemplateLoading(new File("data"));

            // прочие стандартные настройки о них читать тут
            // https://freemarker.apache.org/docs/pgui_quickstart_createconfiguration.html
            cfg.setDefaultEncoding("UTF-8");
            cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            cfg.setLogTemplateExceptions(false);
            cfg.setWrapUncheckedExceptions(true);
            cfg.setFallbackOnNullLoopVariable(false);
            return cfg;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void renderTemplate(HttpExchange exchange, String templateFile, Object dataModel) {
        try {
            // загружаем шаблон из файла по имени.
            // шаблон должен находится по пути, указанном в конфигурации
            Template temp = freemarker.getTemplate(templateFile);

            // freemarker записывает преобразованный шаблон в объект класса writer
            // а наш сервер отправляет клиенту массивы байт
            // по этому нам надо сделать "мост" между этими двумя системами

            // создаём поток который сохраняет всё, что в него будет записано в байтовый массив
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            // создаём объект, который умеет писать в поток и который подходит для freemarker
            try (OutputStreamWriter writer = new OutputStreamWriter(stream)) {

                // обрабатываем шаблон заполняя его данными из модели
                // и записываем результат в объект "записи"
                temp.process(dataModel, writer);
                writer.flush();

                // получаем байтовый поток
                var data = stream.toByteArray();

                // отправляем результат клиенту
                sendByteData(exchange, ResponseCodes.OK, ContentType.TEXT_HTML, data);
            }
        } catch (IOException | TemplateException e) {
            e.printStackTrace();
        }
    }
}
