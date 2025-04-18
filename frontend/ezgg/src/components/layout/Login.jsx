import React, {useState} from 'react';
import {useNavigate} from 'react-router-dom';
import axios from 'axios';
import '../../styles/Login.css';

function Login() {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();

        try {
            const response = await axios.post('/api/login', {
                memberUsername: username,
                password: password
            });

            const token = response.headers['authorization'];
            if (response.status === 200) {
                alert('로그인에 성공했습니다!');
                // 로그인 성공 후, JWT 토큰을 로컬 스토리지에 저장
                localStorage.setItem('token', token);
                // 로그인 성공 후, 홈 페이지로 리다이렉트
                navigate('/');
            }
        } catch (error) {
            alert('로그인에 실패했습니다. 아이디와 비밀번호를 확인해주세요.');
            console.error('Login error:', error);
        }
    };

    return (
        <form className="login-form" onSubmit={handleSubmit}>
            <div className="form-group">
                <label htmlFor="username">아이디</label>
                <input
                    id="username"
                    type="text"
                    placeholder="아이디를 입력하세요"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    required
                />
            </div>
            <div className="form-group">
                <label htmlFor="password">비밀번호</label>
                <input
                    id="password"
                    type="password"
                    placeholder="비밀번호를 입력하세요"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                />
            </div>
            <button type="submit" className="login-button">로그인</button>
        </form>
    );
}

export default Login;
