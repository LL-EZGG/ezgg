import {createBrowserRouter} from 'react-router-dom';
import Layout from '../components/Layout';
import Home from '../pages/HomePage';
import Login from '../pages/LoginPage';
import Join from '../pages/JoinPage';

const router = createBrowserRouter([
    {
        path: '/',
        element: <Layout/>,
        children: [
            {
                index: true,
                element: <Home/>
            },
            {
                path: 'login',
                element: <Login/>
            },
            {
                path: 'join',
                element: <Join/>
            }
        ]
    }
]);

export default router;
