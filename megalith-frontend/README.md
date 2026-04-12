# megalith-frontend

A modern blog frontend built with Vue 3 and Vite, featuring real-time collaborative editing, full-text search, and comprehensive content management.

## ✨ Features

- 📝 **Blog System** - Read and manage blog articles with Markdown support
- ✏️ **Real-time Collaborative Editing** - Built on Yjs and WebSocket for simultaneous multi-user editing
- 🔍 **Full-text Search** - Search content with syntax highlighting
- 💬 **Comment System** - Supports Disqus and Giscus
- 📊 **Reading Statistics** - Track article views and popular content
- 🔐 **Admin Panel** - Complete content management with role-based access control
- 🎨 **Responsive Design** - Element Plus UI components
- 🌐 **i18n Support** - Chinese language support
- 📡 **OpenTelemetry** - Distributed tracing with traceparent header propagation
- 🔒 **Sensitive Content Filter** - Real-time content moderation

## 🛠️ Tech Stack

| Category | Technology |
|----------|------------|
| Framework | Vue 3 (Composition API) |
| Build Tool | Vite |
| UI Library | Element Plus |
| Markdown Editor | md-editor-v3 |
| Collaborative Editing | Yjs + y-websocket |
| State Management | Pinia |
| Routing | Vue Router |
| HTTP Client | Axios |
| Language | TypeScript |

## 🚀 Quick Start

### Prerequisites

- Node.js >= 16
- npm or yarn

### Installation

```bash
# Clone the repository
git clone https://github.com/mingchiuli/megalith-frontend.git
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
├── components/          # Reusable Vue components
│   ├── sys/              # System components (editor, headers, menus)
│   ├── DiscussItem.vue   # Comment component
│   ├── HotItem.vue       # Popular articles widget
│   ├── SearchItem.vue    # Search component
│   └── CatalogueItem.vue # Table of contents
├── views/                # Page components
│   ├── sys/              # Admin panel pages
│   ├── BlogView.vue      # Article reading page
│   ├── BlogsView.vue     # Article listing
│   └── LoginView.vue     # Authentication
├── stores/               # Pinia state stores
├── router/               # Vue Router configuration
├── http/                 # API utilities and Axios config
├── config/               # Application configuration
│   ├── editorConfig.ts   # Yjs collaborative editing setup
│   └── otel.ts           # OpenTelemetry trace context
├── type/                 # TypeScript type definitions
├── utils/                # Utility functions
└── assets/              # Static assets and styles
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
- Browse articles at `/blogs`
- Read individual posts at `/blog/:id`
- Full-text search with highlighting
- View reading statistics and popular articles

### Admin Features
- Login at `/login`
- Rich text editor with real-time collaboration
- Content management at `/sys/blogs`
- User and role management at `/sys/authority`
- System statistics at `/sys`

## 🔍 Notable Implementation

### Real-time Collaborative Editing

The editor uses Yjs CRDT framework with WebSocket provider, enabling multiple authors to edit simultaneously with conflict-free merging.

### OpenTelemetry Integration

Requests to the backend include `traceparent` headers for distributed tracing across services.

### Sensitive Content Filtering

The editor supports real-time content selection and filtering for sensitive material review.

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License.

## 🔗 Related

- Backend API: [megalith-backend](https://github.com/mingchiuli/megalith-backend)
- Live Demo: [chiu.wiki](https://chiu.wiki)
