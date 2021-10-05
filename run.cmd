@echo build maven project
call mvnw clean package
@echo run project
call java -jar target/newsposter-1.0.jar