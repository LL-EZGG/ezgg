import '../styles/Footer.css';

function Footer() {
  return (
    <footer className="footer">
      <div className="footer-content">
        <p>&copy; {new Date().getFullYear()} EZGG. All rights reserved.</p>
      </div>
    </footer>
  );
}

export default Footer; 