import { useState } from 'react';
import axios from 'axios';
import '../../styles/Join.css';
import { useNavigate } from 'react-router-dom';


function Join() {

  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    memberUsername: '',
    password: '',
    email: '',
    riotUsername: '',
    riotTag: ''
  });
  
  const [message, setMessage] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    console.log('회원가입 데이터:', formData);
    
    setIsLoading(true);
    setMessage('');
    
    try {
      const response = await axios.post('/api/auth/signup', formData);
      console.log('회원가입 성공:', response.data);
      setMessage('회원가입이 성공적으로 완료되었습니다!');
      navigate("/");
    } catch (error) {
      console.error('회원가입 오류:', error);
      setMessage(error.response?.data?.message || '회원가입 중 오류가 발생했습니다.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="join-container">
      <form onSubmit={handleSubmit} className="join-form">
        {message && <div className={`message ${message.includes('성공') ? 'success' : 'error'}`}>{message}</div>}
        
        <div className="form-group">
          <label htmlFor="memberUsername">아이디</label>
          <input
            type="text"
            id="memberUsername"
            name="memberUsername"
            value={formData.memberUsername}
            onChange={handleChange}
            required
            disabled={isLoading}
          />
        </div>
        
        <div className="form-group">
          <label htmlFor="password">비밀번호</label>
          <input
            type="password"
            id="password"
            name="password"
            value={formData.password}
            onChange={handleChange}
            required
            disabled={isLoading}
          />
        </div>
        
        <div className="form-group">
          <label htmlFor="email">이메일</label>
          <input
            type="email"
            id="email"
            name="email"
            value={formData.email}
            onChange={handleChange}
            required
            disabled={isLoading}
          />
        </div>
        
        <div className="form-group">
          <label htmlFor="riotUsername">라이엇 아이디</label>
          <input
            type="text"
            id="riotUsername"
            name="riotUsername"
            value={formData.riotUsername}
            onChange={handleChange}
            required
            disabled={isLoading}
          />
        </div>
        
        <div className="form-group">
          <label htmlFor="riotTag">라이엇 태그</label>
          <input
            type="text"
            id="riotTag"
            name="riotTag"
            value={formData.riotTag}
            onChange={handleChange}
            required
            disabled={isLoading}
          />
        </div>
        
        <button type="submit" className="submit-btn" disabled={isLoading}>
          {isLoading ? '처리 중...' : '가입하기'}
        </button>
      </form>
    </div>
  );
}

export default Join; 