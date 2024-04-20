import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { ProjectComponent } from './components/project/project.component';
import { AuthGuard } from './guards/auth.guard';
import { ResetPasswordComponent } from './components/reset-password/reset-password.component';
import { SignupComponent } from './components/signup/signup.component';

export const routes: Routes = [

    { path: 'login', component: LoginComponent , data: { title: 'Login' }
},

{ path: 'signup', component: SignupComponent , data: { title: 'Signup' }
},

{ path: 'forgetPassword', component: ResetPasswordComponent , data: { title: 'forgetPassword' }
},
    { path: 'projects', component: ProjectComponent,canActivate: [AuthGuard] , data: { title: 'Project' }
},
    {
        path: '**',
        redirectTo: 'login'
      }
];
