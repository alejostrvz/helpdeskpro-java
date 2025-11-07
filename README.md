# HelpDeskPro – Sistema de Gestión de Tickets

HelpDeskPro es un proyecto para la materia de Progrmacion 2 de la universidad, es una aplicación desarrollada en Java que permite la gestión integral de solicitudes de soporte técnico (tickets).  
El sistema está dividido en dos módulos principales: un backend desarrollado con Spring Boot y un frontend de escritorio desarrollado con Java Swing.

Su objetivo es ofrecer una solución eficiente para registrar, asignar y resolver incidencias dentro de una organización, ya sea académica o empresarial.

---

## 1. Características Generales

### Frontend (Java Swing)
- Interfaz de usuario intuitiva y funcional.
- Inicio de sesión con autenticación hacia la API.
- Visualización de tickets activos, pendientes y cerrados.
- Reasignación de tickets por parte de administradores.
- Soporte para diferentes roles: Administrador, Técnico y Cliente.

### Backend (Spring Boot)
- API RESTful estructurada bajo el patrón MVC.
- Gestión completa de usuarios, roles, tickets y mensajes.
- Integración con base de datos MySQL.
- Eliminación lógica y reasignación de tickets.
- Transacciones controladas con `@Transactional`.
- Control de dependencias con Maven.

---

## 2. Tecnologías Utilizadas

| Componente | Tecnología | Descripción |
|-------------|-------------|-------------|
| Lenguaje principal | Java 21 | Lenguaje base del proyecto. |
| Framework Backend | Spring Boot 3.x | Framework para desarrollo rápido de APIs REST. |
| Frontend | Java Swing | Interfaz de usuario de escritorio. |
| ORM | Hibernate / Spring Data JPA | Mapeo objeto-relacional. |
| Base de datos | MySQL | Sistema de gestión de base de datos. |
| Compilador | Maven | Gestión de dependencias y empaquetado. |
| IDE recomendado | IntelliJ IDEA o NetBeans | Entorno de desarrollo. |

---

## 3. Estructura del Proyecto
```
helpdeskoro-java/
│
├── helpdesk-backend/
│   ├── src/main/java/com/helpdeskoro/helpdesk/
│   │   ├── controller/          # Controladores REST
│   │   ├── entity/               # Entidades JPA
│   │   ├── repository/           # Interfaces de acceso a datos
│   │   ├── service/              # Lógica de negocio
│   │   ├── scheduler/            # Tareas automáticas (opcional)
│   │   └── HelpDeskProApplication.java
│   │
│   └── src/main/resources/
│       ├── application.properties
│       ├── static/
│       └── templates/
│
├── helpdeskUI/
│   ├── src/main/java/com/helpdeskoro/ui/
│   │   ├── view/                 # Formularios e interfaces gráficas
│   │   └── Main.java             # Punto de entrada del sistema
│   │
│   └── pom.xml
│
├── .gitignore
└── README.md
```


---

## 4. Configuración del Sistema

### Requisitos previos
- Java 21 o superior
- Maven 3.9+
- MySQL Server

### Configuración del Backend
Editar el archivo `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/helpdeskpro
spring.datasource.username=root
spring.datasource.password=tu_contraseña
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
server.port=8080
```

## 5. Ejecución del proyecto

Backend (Spring-Boot)
```
cd helpdesk-backend
mvnw clean install
mvnw spring-boot:run
```

Frontend (Java Swing)
```
cd helpdeskUI
mvnw clean install
java -jar target/helpdeskUI.jar
```

## 6. Credenciales de prueba

| Rol           | Usuario                                             | Contraseña |
| ------------- | --------------------------------------------------- | ---------- |
| Administrador | [admin@helpdesk.com](mailto:admin@helpdesk.com)     | admin123   |
| Técnico       | [tecnico@helpdesk.com](mailto:tecnico@helpdesk.com) | tecnico123 |

## 7. Documentación de Endpoints

Autenticación

| Método | Endpoint          | Descripción                                                 |
| :----- | :---------------- | :---------------------------------------------------------- |
| `POST` | `/api/auth/login` | Inicia sesión de usuario y retorna token o datos de sesión. |


Usuarios
| Método   | Endpoint                        | Descripción                                  |
| :------- | :------------------------------ | :------------------------------------------- |
| `GET`    | `/api/usuarios`                 | Obtiene la lista de todos los usuarios.      |
| `GET`    | `/api/usuarios/{id}`            | Obtiene un usuario por ID.                   |
| `GET`    | `/api/usuarios/correo/{correo}` | Busca un usuario por correo electrónico.     |
| `POST`   | `/api/usuarios`                 | Crea un nuevo usuario.                       |
| `PUT`    | `/api/usuarios/{id}`            | Actualiza los datos de un usuario existente. |
| `DELETE` | `/api/usuarios/{id}`            | Elimina un usuario y sus tickets asociados.  |


Tickets
| Método   | Endpoint            | Descripción                                     |
| :------- | :------------------ | :---------------------------------------------- |
| `GET`    | `/api/tickets`      | Lista todos los tickets del sistema.            |
| `GET`    | `/api/tickets/{id}` | Muestra la información de un ticket específico. |
| `POST`   | `/api/tickets`      | Crea un nuevo ticket (cliente).                 |
| `PUT`    | `/api/tickets/{id}` | Actualiza el estado o información del ticket.   |
| `DELETE` | `/api/tickets/{id}` | Elimina un ticket.                              |


Mensajes
| Método | Endpoint                    | Descripción                                 |
| :----- | :-------------------------- | :------------------------------------------ |
| `GET`  | `/api/mensajes/ticket/{id}` | Obtiene los mensajes asociados a un ticket. |
| `POST` | `/api/mensajes`             | Crea un nuevo mensaje dentro de un ticket.  |


## 8. Base de Datos
Relaciones principales:

Un Usuario puede tener múltiples Tickets.

Un Ticket puede tener múltiples Mensajes.

Cada Mensaje pertenece a un Usuario (autor) y a un Ticket.

Entidades principales:

Usuario: id, nombre, correo, rol, contraseña.

Ticket: id, título, descripción, estado, cliente, técnico asignado.

Mensaje: id, texto, fecha, autor, ticket.

