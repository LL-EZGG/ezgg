import { Link } from 'react-router-dom';
import '../styles/Header.css';

function Header() {
  return (
    <header className="header">
      <div className="logo">
        <Link to="/">EZGG</Link>
      </div>
      <nav className="nav">
        <ul className="menu">
          <li><Link to="/">Home</Link></li>
          <li><Link to="/login">Login</Link></li>
          <li><Link to="/join">Join</Link></li>
        </ul>
      </nav>
    </header>
  );
}

export default Header; 