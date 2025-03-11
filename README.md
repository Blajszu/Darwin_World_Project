# Darwin World Simulation 🌿🐾  

A Java-based desktop application that simulates an ecosystem inspired by Charles Darwin’s theory of natural selection. Users can customize various parameters, observe real-time interactions within the environment, and analyze statistical data.  

This project was developed as part of an Object-Oriented Programming course at AGH University of Krakow by:  
- **Oskar Blajsz** [[Blajszu]](https://github.com/Blajszu)
- **Jagoda Kurosad** [[jagodakurosad]](https://github.com/jagodakurosad)

![Simulation Preview]()  

Detailed project guidelines (in Polish) are available [here](./project_description/). The implemented version follows the **F1 variant**.  

---

## 🌍 Overview  

The simulation represents a world populated by herbivores that roam in search of food to maintain their energy levels and find partners to reproduce. Over time, natural selection and genetic inheritance shape the species, influencing their behaviors and survival strategies.  

The world consists of a **grid-based map** with two primary terrain types:  
- **Steppes** – where plants grow sparsely.  
- **Jungle areas** – where vegetation regenerates quickly, creating a food-rich environment.  

Each animal possesses an **energy reserve**, which depletes over time. Survival depends on:  
✔️ **Finding and consuming plants** to regain energy.  
✔️ **Navigating the map** based on genetic instructions.  
✔️ **Reproducing** if energy levels are high enough.  

### 🔄 Daily Cycle in the Simulation:  
✔️ **Removal of deceased animals** from the ecosystem.  
✔️ **Animals decide their movements** based on genetic instructions.  
✔️ **Plants are consumed** by animals.  
✔️ **Breeding occurs** between animals on the same tile with sufficient energy.  
✔️ **New plants grow** in available locations.  

This process continuously reshapes the population, providing insights into evolutionary mechanisms.  

---

## ⚙️ Features  

### 🔧 Customization  
The application offers **extensive configurability**. Users can tweak various settings or use pre-configured scenarios.  

![Configuration Panel]()  

Parameters available for modification:  
- **Map size** (height & width)  
- **Initial number of animals**  
- **Genome length** (number of genes controlling movement)  
- **Mutation range** (min/max mutations per offspring)
- **Mutation variant**:  
  - **Complete randomness** – Genes mutate completely randomly.
  - **Slight correction** – Genes mutate by only going up or down by one.
- **Starting number of plants**  
- **Daily plant growth rate**  
- **Plant growth pattern**:  
  - **Equatorial forest** – Plants prefer growing in central regions.  
  - **Moving jungle** – New plants tend to grow near existing ones.  
- **Energy system**:  
  - Initial animal energy  
  - Energy required for reproduction  
  - Energy cost per movement  
  - Energy gained from eating plants  

---

### 📜 Presets  
The app includes **predefined simulation setups** for quick experimentation. Users can also **save and load** custom configurations as CSV files.  

> [!NOTE]
> Saved presets are stored in the... TODO  

![Preset Example]()  

---

### 🎨 Real-Time Visualization  
The simulation provides **live graphical feedback**, allowing users to track the population's evolution.  

![Simulation Running]()  

---

### 📊 Data Logging & Analysis  
Users can enable **automatic logging** of simulation data to CSV files. Logged information includes:  
✔️ Total number of animals.  
✔️ Number of plants.  
✔️ Free spaces on the map.  
✔️ Most common genetic sequences.  
✔️ Average energy levels.  
✔️ Average lifespan.  
✔️ Average number of offspring per animal.  

> [!NOTE]
> Logs are saved in the... TODO

---

## 🚀 How to Run  

### 💻 System Requirements  
- **Java 21** must be installed on your system.  
- Download the latest Java version from the official Java website if needed.  

> ✅ **Compatibility:** Tested on **Windows** and **Linux**, should also work on **MacOS**.  

### ▶️ Running the Application  
1️⃣ Download the latest version from the project repository.  
2️⃣ Open a terminal and navigate to the folder where the `.jar` file is located.  
3️⃣ Run the following command:  

```shell
java -jar Darwin_World-v1.0.jar
```
Alternatively, **double-click** the `.jar` file to launch the application.  

---
