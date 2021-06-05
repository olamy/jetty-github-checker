/*
 *  ========================================================================
 *  Copyright (c) 1995-2016 Mort Bay Consulting Pty. Ltd.
 *  ------------------------------------------------------------------------
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *      The Eclipse Public License is available at
 *      http://www.eclipse.org/legal/epl-v10.html
 *
 *      The Apache License v2.0 is available at
 *      http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 *  ========================================================================
 */

package org.mortbay.jetty.github;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.extras.okhttp3.OkHttpConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class GitHubPRsResolver
{
    private static final Logger LOG = LoggerFactory.getLogger(GitHubPRsResolver.class);
    private final String repoName;
    private Path cacheDirectory;
    private GitHub github;

    public GitHubPRsResolver(String repoName) throws IOException
    {
        this.repoName = repoName;
        Path userHome = Paths.get(System.getProperty("user.home"));
        cacheDirectory = userHome.resolve(".cache/github/jetty");
        if (!Files.exists(cacheDirectory))
        {
            Files.createDirectories(cacheDirectory);
        }

        Cache cache = new Cache(cacheDirectory.toFile(), 10 * 1024 * 1024); // 10MB cache

        this.github = GitHubBuilder.fromEnvironment()
            .withConnector(new OkHttpConnector(new OkHttpClient.Builder().cache(cache).build()))
            .build();

        if (this.github.isCredentialValid())
        {
            return;
        }
        else // try with properties file
        {
            this.github = GitHubBuilder.fromPropertyFile().withConnector(
                new OkHttpConnector(new OkHttpClient.Builder().cache(cache).build())).build();

            // Test access
            if (!this.github.isCredentialValid())
            {
                this.github = null;
                throw new IOException("Unable to access github, invalid credentials in ~/.github ?");
            }
        }
        // list current rate limits
        LOG.info("Github API Rate Limits: {}", this.github.getRateLimit());
    }

    public List<GHPullRequest> getOpenPRs() throws IOException
    {
        try
        {
            return github.getRepository(repoName).getPullRequests(GHIssueState.OPEN);
        }
        catch (FileNotFoundException fnfe)
        {
            LOG.warn("error list PRs", fnfe);
            return null;
        }
    }
}
