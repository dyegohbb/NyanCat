def get_target_workflow(repository_name, branch_name):
    workflows = get_workflows(repository_name)

    if not workflows:
        print(f" No workflows found")
        repo_dao.update_execution_status(
            repository_name,
            branch_name,
            "N/A",
            'ERROR',
            None,
            None,
            None,
            "No workflows found"
        )
        return None

    target_workflow = None
    for workflow in workflows:
        if workflow.get('name') == 'Caller CI/CD snapshot':
            target_workflow = workflow
            break

    if target_workflow:
        state = target_workflow.get('state', 'inactive')

        if state.lower() == 'active':
            print(f" Found target workflow: Caller CI/CD snapshot")
            print(f" ID: {target_workflow.get('id', 'N/A')}")
            print(f" State: {state}")
            print(f" File: {target_workflow.get('path', 'N/A')}")
            return target_workflow
        else:
            print(f" Found target workflow: Caller CI/CD snapshot but is inactive")
            repo_dao.update_execution_status(
                repository_name,
                branch_name,
                "N/A",
                'ERROR',
                None,
                None,
                None,
                "Target workflow is inactive"
            )
    else:
        available_workflows = ', '.join([w.get('name', 'N/A') for w in workflows])
        obs = f"Caller CI/CD snapshot workflow not found"

        print(f" WARNING: {obs}")
        print(f" Available workflows: {available_workflows}")
        repo_dao.update_execution_status(
            repository_name,
            branch_name,
            "N/A",
            'ERROR',
            None,
            None,
            None,
            obs
        )

    return None


def run_pipeline(target_workflow, repository_name, branch_name, last_git_commit):
    print(f" Processing workflow execution for: {target_workflow.get('name', 'N/A')}")

    workflow_path = target_workflow.get('path', '')
    workflow_content = get_workflow_content(repository_name, workflow_path)

    if not workflow_content:
        print(f" ERROR: Could not retrieve workflow file content")
        repo_dao.update_execution_status(
            repository_name,
            branch_name,
            "N/A",
            'ERROR',
            None,
            None,
            None,
            "Could not retrieve workflow file content"
        )
        return False

    inputs = parse_workflow_inputs(workflow_content)

    if not inputs:
        print(f" No workflow_dispatch inputs found")
        repo_dao.update_execution_status(
            repository_name,
            branch_name,
            "N/A",
            'ERROR',
            None,
            None,
            None,
            "No workflow_dispatch inputs found"
        )
        return False

    print(f" Found {len(inputs)} workflow dispatch input(s)")

    required_inputs = ['enabled-fortify', 'enabled-sonar', 'enabled-deploy', 'java-version']
    found_inputs = {}

    for input_name, input_config in inputs.items():
        if input_name in required_inputs:
            found_inputs[input_name] = input_config

    missing_inputs = [inp for inp in required_inputs if inp not in found_inputs]

    if missing_inputs:
        missing_str = ','.join(missing_inputs)
        print(f" WARNING: Missing required inputs for pipeline execution: {missing_str}")
        repo_dao.update_execution_status(
            repository_name,
            branch_name,
            "N/A",
            'ERROR',
            None,
            None,
            None,
            f"Missing required inputs: {missing_str}"
        )
        return False

    pipeline_config = {
        'enabled-fortify': True,
        'enabled-sonar': True,
        'enabled-deploy': False,
        'java-version': 'default'
    }

    print(f" SUCCESS: All required inputs found")
    print(f" Pipeline configuration ready: enabled-fortify: {pipeline_config['enabled-fortify']}; enabled-sonar: {pipeline_config['enabled-sonar']}; enabled-deploy: {pipeline_config['enabled-deploy']}; java-version: {pipeline_config['java-version']}")

    workflow_id = target_workflow.get('id')
    success = trigger_workflow_dispatch(repository_name, workflow_id, branch_name, pipeline_config)

    run_id = "NOT FOUND"

    if success:
        print(f" Pipeline execution initiated successfully")

        print(f" Waiting for workflow run to appear...")
        time.sleep(5)

        workflow_run_info = get_latest_workflow_run_by_actor(repository_name, workflow_id, branch_name, "dyego-h-barbosa-bradesco")
        action_run_url = None
        workflow_config_from_github = pipeline_config

        if workflow_run_info:
            action_run_url = workflow_run_info.get('run_url')
            run_id = workflow_run_info.get('run_id')

            print(f" Workflow run URL: {action_run_url}")

        updated = repo_dao.update_execution_status(
            repository_name,
            branch_name,
            last_git_commit,
            'RUNNING',
            action_run_url,
            workflow_config_from_github,
            run_id
        )

        if updated:
            print(f" INFO: Database updated successfully")
        else:
            print(f" WARNING: Failed to update database")

        return True
    else:
        print(f" Failed to initiate pipeline execution")
        repo_dao.update_execution_status(
            repository_name,
            branch_name,
            "N/A",
            'ERROR',
            None,
            None,
            None,
            "Failed to initiate pipeline execution"
        )
        return False


def get_latest_workflow_run_by_actor(repository_name, workflow_id, branch_name, actor):
    url = f"https://api.github.com/repos/{ORG}/{repository_name}/actions/workflows/{workflow_id}/runs"
    headers = {"Authorization": f"token {GITHUB_TOKEN}"}
    params = {
        "branch": branch_name,
        "actor": actor,
        "per_page": 10
    }

    max_attempts = 20

    for attempt in range(max_attempts):
        try:
            response = session.get(url, headers=headers, params=params, timeout=30)

            if response.status_code == 200:
                runs_data = response.json()
                runs = runs_data.get('workflow_runs', [])

                for run in runs:
                    run_status = run.get('status', '').lower()
                    run_actor = run.get('actor', {}).get('login', '')

                    if (run_actor == actor and
                        run_status in ['in_progress', 'queued', 'waiting']):

                        run_id = run.get('id')
                        run_url = run.get('html_url')

                        return {
                            'run_id': run_id,
                            'run_url': run_url,
                            'status': run_status
                        }

                if attempt < max_attempts - 1:
                    print(f" Attempt {attempt + 1}/{max_attempts}: No workflow run found, retrying...")
                    time.sleep(5)
                else:
                    print(f" All {max_attempts} attempts failed to find workflow run")
                    print(url)

        except Exception as e:
            print(f" ERROR on attempt {attempt + 1}: Failed to get workflow run by actor: {str(e)[:100]}")
            if attempt < max_attempts - 1:
                time.sleep(5)

    return None


def get_last_commit(repo, branch_name):
    url = f"https://api.github.com/repos/{ORG}/{repo}/commits/{branch_name}"
    headers = {"Authorization": f"token {GITHUB_TOKEN}"}

    try:
        response = session.get(url, headers=headers, timeout=30)

        if response.status_code == 200:
            commit_data = response.json()
            commit_sha = commit_data.get('sha', 'N/A')
            return f"{commit_sha}"
        else:
            return "N/A"

    except Exception as e:
        return "N/A"


if __name__ == "__main__":
    print("INFO: Starting pipeline execution process...")
    process_repositories()
    print("\nINFO: Pipeline execution process completed!")