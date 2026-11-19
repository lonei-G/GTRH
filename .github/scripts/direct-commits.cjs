const { execFileSync } = require('node:child_process');
const { existsSync } = require('node:fs');

module.exports = async function addDirectCommits(github, context) {
    const tag = context.ref.slice('refs/tags/'.length);
    if (existsSync(`.changelogs/${tag}.md`)) return;

    const repo = context.repo;
    const { data: release } = await github.rest.repos.getReleaseByTag({ ...repo, tag });
    const body = release.body || '';
    const marker = '<!-- gtrh-direct-commits -->';
    if (body.includes(marker)) return;

    const url = `${context.serverUrl}/${repo.owner}/${repo.repo}`;
    const compareLine = body.split(/\r?\n/).find(line => line.startsWith(`**Full Changelog**: ${url}/compare/`));
    if (!compareLine) return;
    const range = decodeURIComponent(compareLine.slice(`**Full Changelog**: ${url}/compare/`.length).trim());
    const base = range.slice(0, range.indexOf('...'));
    if (!base || range !== `${base}...${tag}`) throw new Error(`Unexpected release range: ${range}`);

    const hashes = execFileSync('git', [
        'log', '--reverse', '--no-merges', '--format=%H', `refs/tags/${base}..refs/tags/${tag}`, '--'
    ], { encoding: 'utf8' }).trim();
    if (!hashes) return;

    const entries = [];
    for (const sha of hashes.split(/\r?\n/)) {
        const pulls = await github.paginate(github.rest.repos.listPullRequestsAssociatedWithCommit, {
            ...repo, commit_sha: sha, per_page: 100
        });
        if (pulls.some(pr => pr.merged_at && pr.base.repo.full_name === `${repo.owner}/${repo.repo}`)) continue;
        const { data } = await github.rest.repos.getCommit({ ...repo, ref: sha });
        const author = data.author ? `@${data.author.login}` : data.commit.author.name;
        entries.push(`* ${data.commit.message.split('\n')[0]} by ${author} in [\`${sha.slice(0, 7)}\`](${url}/commit/${sha})`);
    }
    if (!entries.length) return;

    const heading = body.includes("## What's Changed") ? '## Direct Commits' : "## What's Changed";
    const section = [marker, heading, ...entries, ''].join('\n');
    const updated = body.replace(compareLine, () => section + '\n' + compareLine);
    await github.rest.repos.updateRelease({ ...repo, release_id: release.id, body: updated });
};
