package ru.stqa.pft.rest;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.message.BasicNameValuePair;
import org.testng.SkipException;

import java.io.IOException;
import java.util.Set;

public class TestBase {

    public Set<Issue> getIssues() throws IOException {
        String json = getExecuter().execute(Request.Get("https://bugify.stqa.ru/api/issues.json?limit=500"))
                .returnContent().asString();
        JsonElement parsed = JsonParser.parseString(json);
        JsonElement issues = parsed.getAsJsonObject().get("issues");
        return new Gson().fromJson(issues, new TypeToken<Set<Issue>>(){}.getType());
    }

    public Executor getExecuter() {
        return Executor.newInstance().auth("288f44776e7bec4bf44fdfeb1e646490", "");
    }

    public int createIssue(Issue newIssue) throws IOException {
        String json = getExecuter().execute(Request.Post("https://bugify.stqa.ru/api/issues.json?limit=500")
                .bodyForm(new BasicNameValuePair("subject", newIssue.getSubject()),
                        new BasicNameValuePair("description", newIssue.getDescription())))
                .returnContent().asString();
        JsonElement parsed = JsonParser.parseString(json);
        return parsed.getAsJsonObject().get("issue_id").getAsInt();
    }

    private Set<Issue> getIdIssue(int issueId) throws IOException {
        String json = getExecuter().execute(Request.Get(String.format("https://bugify.stqa.ru/api/issues/%s.json?limit=500",issueId)))
                .returnContent().asString();
        JsonElement parsed = JsonParser.parseString(json).getAsJsonObject().get("issues");
        return new Gson().fromJson(parsed, new TypeToken<Set<Issue>>() {}.getType());
    }

    public boolean isIssueOpen(int issueId) throws IOException {
        Issue issue = getIdIssue(issueId).iterator().next();
        return !(issue.getState_name().equals("Resolved") || issue.getState_name().equals("Closed") || issue.getState_name().equals("Deleted"));
    }

    public void skipIfNotFixed(int issueId) throws IOException {
        if (isIssueOpen(issueId)) {
            throw new SkipException("Ignored because of issue " + issueId);
        }
    }

}
