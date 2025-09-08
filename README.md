# LEARNnow Adaptive Learning Backend

A production-grade Spring Boot backend system that transforms course descriptions into complete learning journeys with videos, quizzes, and performance analytics.

## 🌟 Features

- **Course Creation Pipeline**: Transforms course descriptions into structured learning content
- **AI-Powered Content Generation**: Uses Groq API for course summarization and quiz generation
- **YouTube Integration**: Searches for educational videos with caption filtering 
- **Adaptive Learning Analytics**: Calculates learning rates based on performance and difficulty
- **RESTful APIs**: Complete CRUD operations with comprehensive error handling
- **Production Ready**: Proper layered architecture with tests and monitoring

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+
- MySQL 8.0+ (for production) or H2 (for development)

### Running Locally

1. **Clone the repository**
```bash
git clone <repository-url>
cd DreamPipeLine
```

2. **Run with H2 database (development)**
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local"
```

3. **Access the application**
- Application: http://localhost:8080
- H2 Console: http://localhost:8080/h2-console

### Running with MySQL (Production)

1. **Set up MySQL database**
```sql
CREATE DATABASE learnnow_db;
CREATE USER 'learnnow'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON learnnow_db.* TO 'learnnow'@'localhost';
```

2. **Update application.properties**
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/learnnow_db
spring.datasource.username=learnnow
spring.datasource.password=your_password
```

3. **Add API Keys**
```properties
groq.api.key=YOUR_GROQ_API_KEY_HERE
youtube.api.key=YOUR_YOUTUBE_API_KEY_HERE
```

4. **Run the application**
```bash
mvn spring-boot:run
```

## 📡 API Endpoints

### Course Management

#### Create Course
```http
POST /api/courses
Content-Type: application/json

{
  "courseName": "Spring Boot Mastery",
  "courseDescription": "A deep dive into Spring Boot, REST APIs, and microservices architecture.",
  "userId": 1
}
```

#### Get Course
```http
GET /api/courses/{courseId}
```

#### Get User Courses
```http
GET /api/courses/user/{userId}
```

### Quiz Management

#### Submit Quiz
```http
POST /api/quiz/submit
Content-Type: application/json

{
  "userId": 1,
  "topicId": 1,
  "timeTakenMinutes": 15,
  "answers": [
    {
      "quizId": 1,
      "answer": "Option B"
    },
    {
      "quizId": 2,
      "answer": "Option A"
    }
  ]
}
```

#### Get Quiz Results
```http
GET /api/quiz/results?userId=1&topicId=1
```

### User Analytics

#### Get User Progress
```http
GET /api/user/{userId}/progress
```

#### Get Performance Summary
```http
GET /api/user/{userId}/summary
```

#### Get Learning Trend
```http
GET /api/user/{userId}/trend
```

## 🏗️ Architecture

### Layered Architecture
```
┌─────────────────┐
│   Controllers   │ ← REST API endpoints
├─────────────────┤
│    Services     │ ← Business logic
├─────────────────┤
│  Repositories   │ ← Data access
├─────────────────┤
│   Entities      │ ← JPA entities
└─────────────────┘
```

### Pipeline Flow
1. **Course Submission** → Store course and trigger pipeline
2. **Summarization** → Groq API generates course summary
3. **Subtopic Generation** → AI creates structured learning topics
4. **Video Search** → YouTube API finds educational videos with captions
5. **Transcript Extraction** → Fetch video transcripts
6. **Quiz Generation** → AI creates multiple-choice questions
7. **Performance Tracking** → Calculate learning rates and analytics

## 🗄️ Database Schema

### Core Entities
- **User**: Learner information
- **Course**: Course details and summary
- **Topic**: Course subtopics
- **Video**: YouTube videos with captions
- **Quiz**: Generated questions
- **UserPerformance**: Analytics and learning rates

### Relationships
- User ←→ Course (One-to-Many)
- Course ←→ Topic (One-to-Many)
- Topic ←→ Video (One-to-Many)
- Topic ←→ Quiz (One-to-Many)
- User + Topic ←→ UserPerformance (Many-to-One)

## 🔧 Configuration

### API Keys Setup
Create `application-local.properties` for development:
```properties
# Groq API
groq.api.key=YOUR_GROQ_API_KEY_HERE
groq.api.base-url=https://api.groq.com/openai/v1

# YouTube API
youtube.api.key=YOUR_YOUTUBE_API_KEY_HERE
youtube.api.base-url=https://www.googleapis.com/youtube/v3
```

### Application Settings
```properties
# Quiz Configuration
app.quiz.default-questions-per-topic=7
app.quiz.difficulty-factor-base=1.0

# Video Requirements
app.video.caption-required=true
```

## 🧪 Testing

### Run Unit Tests
```bash
mvn test
```

### Run Integration Tests
```bash
mvn test -P integration
```

### Manual Testing with cURL

#### Create a Course
```bash
curl -X POST http://localhost:8080/api/courses \
  -H "Content-Type: application/json" \
  -d '{
    "courseName": "Spring Boot Fundamentals",
    "courseDescription": "Learn Spring Boot from basics to advanced concepts including REST APIs, JPA, and security."
  }'
```

#### Submit a Quiz
```bash
curl -X POST http://localhost:8080/api/quiz/submit \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "topicId": 1,
    "timeTakenMinutes": 10,
    "answers": [
      {"quizId": 1, "answer": "Option B"},
      {"quizId": 2, "answer": "Option C"}
    ]
  }'
```

## 🔍 Monitoring

### Health Check
```http
GET /actuator/health
```

### Application Logs
Logs are configured with SLF4J and include:
- API request/response logging
- External API integration status
- Performance metrics
- Error tracking

## 🚀 Deployment

### Docker (Optional)
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/dream-pipeline-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Environment Variables
```bash
SPRING_PROFILES_ACTIVE=production
GROQ_API_KEY=your_groq_key
YOUTUBE_API_KEY=your_youtube_key
MYSQL_URL=jdbc:mysql://localhost:3306/learnnow_db
MYSQL_USERNAME=learnnow
MYSQL_PASSWORD=your_password
```

## 🎯 Learning Rate Algorithm

The adaptive learning system calculates learning rates using:

```
learningRate = (accuracy / 100) * difficultyFactor
difficultyFactor = baseFactor + (videoLengthMinutes / 60)
```

This provides personalized learning progression based on:
- Quiz performance accuracy
- Content difficulty (video length proxy)
- Individual learning patterns

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Add tests for new functionality
4. Ensure all tests pass
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License.

## 🆘 Support

For issues and questions:
1. Check the [API documentation](#-api-endpoints)
2. Review application logs
3. Open an issue with detailed steps to reproduce

---

**Note**: Replace `YOUR_GROQ_API_KEY_HERE` and `YOUR_YOUTUBE_API_KEY_HERE` with actual API keys before running in production.
