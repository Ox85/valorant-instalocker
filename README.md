# Valorant Instalocker

<img src="https://i.imgur.com/omHJyxg.png">

This tool is a Java-based Valorant agent instalocker that automatically selects your preferred agent during character selection. It interfaces directly with Valorant's API using your local lockfile credentials.

---

## ⚙️ How It Works

- Reads Valorant's lockfile to obtain authentication tokens
- Interfaces with Riot's API to:
  - Verify your agent ownership
  - Get current game state
  - Lock your selected agent
- Features a sleek dark-mode GUI with agent icons
- Organizes agents by their roles (Duelist, Controller, Initiator, Sentinel)
- Only shows agents you own (with Jett, Phoenix, Brimstone, Sova, and Sage enabled by default)

---

## 📁 Setup

### 1. Requirements

- Java 16 or higher
- Valorant must be running (for lockfile access)
- EU region server only (currently supported)

### 2. Dependencies

The tool requires these libraries (automatically handled by Maven):

```xml
<dependencies>
    <dependency>
        <groupId>com.google.code.gson</groupId>
        <artifactId>gson</artifactId>
        <version>2.9.1</version>
    </dependency>
    <dependency>
        <groupId>com.github.weisj</groupId>
        <artifactId>darklaf-core</artifactId>
        <version>3.0.2</version>
    </dependency>
    <dependency>
        <groupId>commons-io</groupId>
        <artifactId>commons-io</artifactId>
        <version>2.11.0</version>
    </dependency>
</dependencies>
```

---

### 🚀 Usage

- Launch Valorant (must be in EU region)
- Run the instalocker tool
- Select your desired agent from the GUI
- Click "Lock" to activate the instalocker
- The tool will automatically select your agent during character selection phase

---

### 🖼️ GUI Features

- Role-based organization: Agents sorted by their in-game roles
- Visual selection: Click agent icons to choose your preference
- Ownership verification: Only shows agents available in your account
- Status indicator: Shows when instalocker is active
- Dark theme: Easy-on-the-eyes interface

---

### 🛡️ Safety Notes

- Only reads Valorant's lockfile (no injection or memory modification)
- Uses official API endpoints
- No persistent authentication - tokens are only used during session
- Automatically checks for maintenance/updates before running

---

### ⚠️ Disclaimer

This project is for educational purposes only. Using third-party tools with Valorant may violate Riot Games' Terms of Service. Use at your own risk. The developer is not responsible for any account penalties that may occur.

---

### :rose: Special Thanks

[@Ox85](https://github.com/Ox85) for contributions and support

---

Note: This tool currently only supports the EU region. Other regions may be added in future updates.
