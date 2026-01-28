import { useState, type ChangeEvent } from 'react'; // Xóa useEffect nếu không dùng để hết lỗi đỏ
import axios from 'axios';
import './App.css'; // Import file CSS đã viết ở bước trên

interface UserSession {
    id: number;
    username: string;
    fullName: string;
}

function App() {
    const [username, setUsername] = useState<string>('');
    const [password, setPassword] = useState<string>('');
    const [message, setMessage] = useState<string>('');

    const [loggedInUser, setLoggedInUser] = useState<UserSession | null>(() => {
        const savedUser = localStorage.getItem('userSession');
        if (savedUser) {
            try { return JSON.parse(savedUser); }
            catch { localStorage.removeItem('userSession'); return null; }
        }
        return null;
    });

    const handleLogin = async () => {
        try {
            const response = await axios.post('http://localhost:8080/api/auth/login', { username, password });
            const userData: UserSession = response.data;
            setLoggedInUser(userData);
            localStorage.setItem('userSession', JSON.stringify(userData));
        } catch (error: unknown) {
            if (axios.isAxiosError(error)) setMessage(error.response?.data || "Lỗi đăng nhập!");
        }
    };

    const handleLogout = () => {
        setLoggedInUser(null);
        localStorage.removeItem('userSession');
    };

    if (loggedInUser) {
        return (
            <div className="app-container">
                <div className="glass-card">
                    <div className="user-avatar">{loggedInUser.fullName.charAt(0)}</div>
                    <h2 className="header">Hệ thống Quản lý</h2>
                    <p>Xin chào chuyên viên,</p>
                    <h3 style={{ margin: '10px 0 25px' }}>{loggedInUser.fullName}</h3>
                    <button onClick={handleLogout} className="logout-btn">Đăng xuất</button>
                </div>
            </div>
        );
    }

    return (
        <div className="app-container">
            <div className="glass-card">
                <h2 className="header">Đăng nhập Hệ thống</h2>
                <div className="input-wrapper">
                    <input className="input-field" type="text" placeholder="Tài khoản" onChange={(e: ChangeEvent<HTMLInputElement>) => setUsername(e.target.value)} />
                    <input className="input-field" type="password" placeholder="Mật khẩu" onChange={(e: ChangeEvent<HTMLInputElement>) => setPassword(e.target.value)} />
                </div>
                <button onClick={handleLogin} className="login-btn">Đăng nhập</button>
                {message && <p className="error-msg">{message}</p>}
            </div>
        </div>
    );
}

export default App;