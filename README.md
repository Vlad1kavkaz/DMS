

# Satellite DB — запуск на Astra Linux

## 1. Установка зависимостей

### 1.1 Установка Java 17

Если репозитории закомментированы:

    sudo nano /etc/apt/sources.list
должно быть так:
<img width="1298" height="267" alt="Снимок экрана 2026-04-04 в 08 08 17" src="https://github.com/user-attachments/assets/41a0142e-0d8e-4d4a-82f5-285ae4f7a31d" />

Раскомментировать нужные строки, затем (без VPN на компе):

    sudo apt update
    sudo apt install openjdk-17-jdk

Проверка:

    java -version
    javac -version

Ожидается Java 17.

---

### 1.2 Установка PostgreSQL (без VPN на компе)

    sudo apt install postgresql

Проверка:

    sudo systemctl status postgresql

Должен быть active (running).

---

### 1.3 Установка JavaFX (без VPN на компе)

    sudo apt install openjfx

---

### 1.4 Установка Git и Maven (без VPN на компе)

    sudo apt update
    sudo apt install git maven

---

## 2. Настройка базы данных

### Вход в postgres

    sudo -i -u postgres
    psql

### Установка пароля

    ALTER USER postgres WITH PASSWORD 'postgres';

### Проверка баз

    \l

### Создание базы

    CREATE DATABASE db_ics_cogs OWNER postgres;

Проверка:

    \l

### Выход

    \q
    exit

---

### Подключение к базе

    psql -h localhost -p 5432 -U postgres -d db_ics_cogs

Пароль:

    postgres

---

### Применение SQL-скрипта

Вставить содержимое файла /sql/init.sql

---

### Проверка

    \dn
    \dt sc_cogs.*

---

## 3. Клонирование проекта

    git clone https://github.com/Vlad1kavkaz/DMS.git
    cd DMS

---

## 4. Сборка

    mvn clean package

---

## 5. Запуск

    mvn javafx:run
