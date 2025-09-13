# megalith-frontend

A modern blog frontend built with Vue 3 and Vite, featuring a clean interface for reading and managing blog content.

## ✨ Features

- 📝 Blog reading with markdown support
- 🔍 Full-text search functionality
- 💬 Comment system (Disqus & Giscus)
- 📊 Reading statistics and hot articles
- 🎨 Responsive design with Element Plus UI
- 🔐 Admin panel for content management
- 🌐 Multi-language support (Chinese)

## 🛠️ Tech Stack

- **Framework**: Vue 3 with Composition API
- **Build Tool**: Vite
- **UI Library**: Element Plus
- **Markdown**: md-editor-v3
- **State Management**: Pinia
- **Routing**: Vue Router
- **Language**: TypeScript

## 🚀 Quick Start

### Prerequisites

- Node.js >= 16
- npm or yarn

### Installation

```bash
# Clone the repository
git clone <your-repo-url>
cd megalith-frontend

# Install dependencies
npm install

# Start development server
npm run dev
```

The application will be available at `http://127.0.0.1:1919`

### Build for Production

```bash
npm run build
```

## 📁 Project Structure

```
src/
├── components/     # Reusable components
├── views/         # Page components
├── stores/        # Pinia stores
├── router/        # Vue Router configuration
├── http/          # API utilities
├── type/          # TypeScript type definitions
├── utils/         # Utility functions
└── assets/        # Static assets
```

## 🔧 Configuration

### Environment Variables

Create `.env.local` for local development:

```env
VITE_BASE_URL='http://localhost:8088'
VITE_BASE_WS_URL='ws://localhost:8088'
```

### API Proxy

The development server proxies API requests:
- `/api/*` → Backend REST API
- `/wsapi/*` → WebSocket connections

## 📖 Usage

### Public Features
- Browse blog articles at `/blogs`
- Read individual posts at `/blog/:id`
- Search content with highlighting
- View reading statistics

### Admin Features
- Login at `/login`
- Manage blogs in admin panel
- View deleted content
- Monitor system statistics

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License.

## 🔗 Related

- Backend API: [megalith-backend](link-to-backend-repo)
- Live Demo: [chiu.wiki](https://chiu.wiki)
