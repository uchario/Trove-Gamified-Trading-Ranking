# Trading Application - Interview Assessment

**All 13 tests pass** (`mvn test` → `BUILD SUCCESS`)

---

## How to Run (Step-by-Step)

### 1. Clone the Repository

```bash
git clone https://github.com/uchario/Trove-Gamified-Trading-Ranking
cd Trove-Gamified-Trading-Ranking
```

### 2. Build the Docker Image

```bash
docker build -t trading-app .
```

### 3. Run the Application

```bash
docker run -p 8080:8080 --name trading-app trading-app
```
