# DiscordBot

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![JDA](https://img.shields.io/badge/JDA-7289DA?style=for-the-badge&logo=discord&logoColor=white)

## 📌 Description
**DiscordBot** is a bot for Discord written in **Java** using **JDA** (Java Discord API). It is designed for psychological support servers, providing ticket management, an economy system, private voice channels, and user rating functionality.

## 🔧 Features
- 🎫 **Ticket System**: Users can create tickets for psychological support. Only users with the "Psychologist" role can claim tickets. A special menu allows psychologists to close or transfer tickets to a "Closed Tickets" category. When a ticket is closed, all messages from it are sent to the log channel.
- 💰 **Economy System**: Users can check their balance using `/stats`. A `/shop` command allows purchasing roles, and administrators can add roles for sale using `/add_role_to_shop [role_id] [price]`.
- 🔊 **Private Voice Channels**: Users can create private voice channels with controls to:
  - Lock the channel from other users
  - Rename the voice channel
  - Set a user limit
  - Remove users from the voice channel
- ⭐ **Feedback & Rating System**: Users can leave reviews and rate psychologists based on their experience.

## 🚀 Installation & Launch

### 1. Clone the Repository
```sh
git clone https://github.com/lolface567/DiscordBot.git
cd DiscordBot
```

### 2. Install Dependencies
Make sure you have **Maven** installed, then run:
```sh
mvn clean install
```

### 3. Configuration
Create a `.env` file in the root folder and specify the following parameters:
```
TOKEN= Bot token
feedbackChannel= Channel for receiving feedback about psychologists
ticketCategory= Category where tickets will be created
adminChannel= Channel where created tickets will be sent
psyhologRole= ID of the psychologist role
closeTicketCategory= Category where closed tickets will be moved
voiceCategory= Category where private voice channels will be created
embedMessage= Channel where the ticket creation menu will be sent
voiceChannelId= Voice channel where, upon joining, a private voice will be created and the user will be moved there
categoryVoiceId= Category where private voice channels will be created
logsChannel= Channel where logs of closed tickets will be sent
systemMessages= Channel where messages about earned coins will be sent
ChannelForSpam= Channel where messages will be counted for earning coins
url= Database connection URL
user= Database username
password= Database password
```

### 4. Run the Bot
```sh
java -jar target/DiscordBot.jar
```

## 📜 Commands
| Command | Description |
|---------|------------|
| `/shop` | Opens the in-bot shop for purchasing roles |
| `/add_role_to_shop [role_id] [price]` | Adds a role for sale in the shop |
| `/stats` | Shows the user's balance and statistics |
| `/add_coins` | Adds coins to a user |
| `/clear-baned-psyholog` | Clears banned psychologists from the system |
| `/create-ticket-sys` | Initializes the ticket system |
| `/rating` | Displays the rating of psychologists |
| `/top` | Shows the top-rated users |
| `/menu` | Opens the psychologist's management menu |
| `/clear-closed-tickets` | Clears closed tickets from the system |

## 🤝 Contribution
If you want to contribute to the project, create a **fork**, make your changes, and submit a **pull request**. Your improvements are welcome!

## 📄 License
This project is distributed under the **MIT** license. See the `LICENSE` file for details.

---
© 2025 lolface567. All rights reserved.

