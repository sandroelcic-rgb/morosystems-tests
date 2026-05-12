# Test assignment for MoroSystems

## Dependencies:

The only dependency is a docker instance (+ active internet connection).  
Everything else should be taken care of in the containers.

## Run:

To run the tests locally from the repository's root use the commands below.

### API tests

```
docker compose up --build -d --wait todo-api
docker compose run --rm api-tests
```

### Browser tests

```
docker compose up -d --wait selenium
docker compose run --rm browser-tests
```

### Cleanup

```
docker compose down -v
```

## Notes:

1. Starting the browser testing on Google homepage was omitted because of a blocking reCAPTCHA. That is one of the reasons, why starting testing on Google is generally not the best practice.
2. In API tests new task was created (the assignment PDF mentions to create a user, but the API doesn't offer this method).
3. Used POST request to edit an existing task (assignment PDF mentions PUT request, which is not available).
4. Screenshot after selenium tests are saved in browser-test/target/screenshots - stored as artifact in the CI run.
5. The repository is ready to be deployed in GitHub where it should start a CI/CD Action pipeline on every pushed change.
6. While the browser tests run, you can watch the Selenium session in noVNC on port 7900.
   - Open `http://localhost:7900/` in your browser and log in with password `pass`.