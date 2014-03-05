package net.jeebiss.spazz.github;

import net.jeebiss.spazz.util.Utilities;

public class PullRequest extends Issue {

    private String merged_at;

    @Override
    public String getUrl() {
        return html_url;
    }

    @Override
    public boolean isPullRequest() {
        return true;
    }

    @Override
    public String getShortUrl() {
        return Utilities.getShortUrl(html_url);
    }

    public boolean merged() {
        return merged_at != null;
    }

}
