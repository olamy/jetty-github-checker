package org.mortbay.jetty.github;

import org.junit.jupiter.api.Test;
import org.kohsuke.github.GHPullRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class GitHubPRsResolverTests
{

    private static final Logger LOGGER = LoggerFactory.getLogger(GitHubPRsResolverTests.class);

    @Test
    public void testAllPRs() throws Exception
    {
        GitHubPRsResolver gitHubPRsResolver = new GitHubPRsResolver("eclipse/jetty.project");
        List<GHPullRequest> prs = gitHubPRsResolver.getOpenPRs();
        prs.forEach(ghPullRequest -> LOGGER.info("{}", ghPullRequest));
    }

}
