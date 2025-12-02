# Syrrak – Mobile Intrusion Detection System (Android Prototype)

**mIDS** is a research-focused **on-device Intrusion Detection System (IDS)** designed specifically for **Android smartphones**, with future support planned for **iOS** and **Linux-based mobile platforms**.
This prototype demonstrates how to detect suspicious or malicious activity occurring over:

* **Wi-Fi**
* **Bluetooth / BLE**
* **Cellular data**
* **Local app-initiated network flows**

The design supports **real-time packet inspection**, **flow/session analysis**, **rule-based detection**, and **lightweight anomaly detection**, while remaining safe, privacy-respectful, and battery-efficient.

> ⚠️ **Disclaimer:**
> mIDS is an experimental security research project. It is **not a VPN** or privacy-routing tool. It captures *local device traffic only* via Android’s `VpnService` TUN interface.
> It does **not** forward traffic or modify routing.

---

## Features (Prototype Phase)

The Android prototype implements:

### **1. VpnService-based packet capture**

* Uses a **TUN interface** to intercept outbound & inbound traffic at the IP layer.
* Works locally; traffic is **never sent to a remote VPN server**.
* Captured packets are processed via Kotlin coroutines for efficient streaming.

### **2. Lightweight packet parsing**

* IPv4 header parsing
* TCP/UDP protocol extraction
* Payload slicing
* Source and destination IP extraction

### **3. Flow/sessionizer**

* Groups packets by 5-tuple `(srcIP, dstIP, srcPort, dstPort, protocol)`
* Tracks:

  * packet counts
  * bytes
  * average packet size
  * inter-arrival times
  * unique destinations
* Generates windowed features for detection pipelines.

### **4. Feature extractor**

* Aggregates flows into 10-second (configurable) statistical windows.
* Prepares feature vectors for:

  * rule engine
  * anomaly detector
  * ML models (optional)

### **5. Rule engine**

Simple starter rules:

* excessive packet rate
* very low inter-arrival times
* potential scanning indicators
* suspicious DNS bursts
* malformed packet ratios

Custom rules can be added easily.

### **6. Anomaly detection engine**

Initial supported modes:

* **Z-score baseline detection** (no training required)
* Optional **TensorFlow Lite model** (autoencoder / IsolationForest)
* Designed to be expandable toward full behavioral modeling.

### **7. BLE scanning engine**

Detects:

* unknown nearby advertisers
* excessively strong or fluctuating RSSI
* suspicious beacon floods
* advertising payload anomalies

### **8. Encrypted alert storage**

* Uses AndroidX **Security Crypto**
* Alert JSON + optional flow metadata stored in AES-256 encrypted files
* Room database stores metadata for UI presentation

### **9. UI Fragments**

* Session summary
* Live capture state
* Alerts list
* Alert details and export options

---

## System Architecture

```mermaid
flowchart LR
  A[VpnService TUN Interface] --> B[PacketReader]
  B --> C[Sessionizer / Flow Table]
  C --> D[Feature Extractor]
  D --> E[Rule Engine]
  D --> F[Anomaly Detector (ML or Z-Score)]
  subgraph BLE
    J[BLE Scanner] --> D
  end
  E --> G[Alert Store (Encrypted)]
  F --> G
  G --> H[UI: Alerts, Export, Settings]
```

---

## Project Structure

```
mIDS/
├─ app/
│  ├─ java/com/example/mids/
│  │  ├─ net/
│  │  │  ├─ CaptureVpnService.kt
│  │  │  ├─ PacketDispatcher.kt
│  │  │  ├─ SimpleIpParser.kt
│  │  │  ├─ FlowSessionizer.kt
│  │  │  ├─ FeatureExtractor.kt
│  │  │  └─ DetectionPipeline.kt
│  │  ├─ bt/
│  │  │  ├─ BleScanner.kt
│  │  ├─ data/
│  │  │  ├─ Room Entities & DAO
│  │  │  ├─ EncryptedStore.kt
│  │  ├─ ui/
│  │  │  ├─ fragments/
│  │  │  │  ├─ SessionFragment.kt
│  │  │  │  ├─ AlertsFragment.kt
│  │  │  │  └─ AlertDetailFragment.kt
│  │  ├─ utils/
│  │  │  └─ RunningStats.kt
│  ├─ AndroidManifest.xml
│  └─ build.gradle
```

---

# Building & Running the Prototype

## **Prerequisites**

* Android Studio Flamingo or later
* Android SDK 34
* Real Android device (recommended — emulator networking is limited)
* Ensure Kernel supports TUN (all modern Android devices do)

## **1. Clone repository**

```bash
git clone https://github.com/<your-user>/mIDS.git
cd mIDS
```

## **2. Open in Android Studio**

Import the Gradle project normally.

## **3. Add permissions to `AndroidManifest.xml`**

Included in template; ensure:

* `BIND_VPN_SERVICE`
* `BLUETOOTH_SCAN`
* `ACCESS_FINE_LOCATION`
* `INTERNET`

## **4. Build & run**

Click **Run ▶️** in Android Studio.

The app will:

1. Ask for **VPN permission**
2. Ask for **Bluetooth scanning permission**
3. Start the capture service
4. Begin analyzing flows

---

# Building a Test Environment

To properly validate the IDS, use a controlled multi-device testbed.

---

## **A. Wi-Fi Attack / Anomaly Simulation**

### 1. **Evil Twin Access Point**

Use a small Linux device (Raspberry Pi or laptop):

```bash
airbase-ng -e "TestAP" -c 6 wlan0
```

Observe:

* DNS spoofing attempts
* unusual SNI
* high SYN retries

### 2. **ARP or ICMP Flooding**

```
arpspoof -t <victim> <gateway>
ping <target> -f
```

mIDS should detect:

* rapid inter-arrival times
* high packet rate

### 3. **Port Scanning (Nmap)**

From another device:

```
nmap -sS <android-ip>
```

Expect detection:

* high SYN-to-ACK ratio
* burst of flows with unique ports

---

## **B. Bluetooth / BLE Attack Simulation**

### 1. BLE Beacon Flood

Using `hcitool`:

```bash
sudo hcitool -i hci0 lescan --duplicates
```

Or custom advertiser spamming tools.

### 2. Rogue BLE Device

Randomize MAC + rotate advertisement UUIDs.
mIDS should flag rapid appearance/disappearance.

---

## **C. Network Behavior Profiling**

Run background apps:

* Spotify
* YouTube
* Telegram
* Instagram

Logging normal traffic allows:

* baseline calculation
* anomaly validation
* model training (optional)

---

# Roadmap

| Phase                  | Goals                                           | Status         |
| ---------------------- | ----------------------------------------------- | -------------- |
| **0.1 Prototype**      | TUN capture, BLE scan, sessionizer, rule engine | ✔ Complete     |
| **0.2 Stability Pass** | Reduce battery usage, Room persistence          | 🕒 In progress |
| **0.3 ML Integration** | Autoencoder or IsolationForest (TFLite)         | Planned        |
| **0.4 iOS Research**   | Evaluate NEPacketTunnelProvider feasibility     | Planned        |
| **0.5 Linux Phones**   | PinePhone / PostmarketOS port                   | Planned        |
| **1.0 Beta Release**   | Hardening, better UI, telemetry opt-in          | Future         |

---

# Security & Privacy Principles

* **No traffic leaves the device by default**
* **User-consent required** for any cloud upload
* **pcap exports sanitized** (hashed IPs unless full export requested)
* **Minimal permissions**
* **No third-party analytics**
* **Encrypted storage for all alerts**

---

# Contributing

Bug reports and PRs are welcome. Please follow the structure:

1. Fork repository
2. Create feature branch
3. Write tests where applicable
4. Ensure code is Kotlin-idiomatic
5. Submit pull request

---

### License

MIT License — permits research, modification, and commercial use with attribution.

