import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { provideHttpClient, withInterceptors, withInterceptorsFromDi } from '@angular/common/http';
import { JwtInterceptor } from './intercepter/JwtInterceptor';


export const appConfig: ApplicationConfig = {
  providers: [provideRouter(routes) ,     
    provideHttpClient(withInterceptors([JwtInterceptor])),

   


  ]
};
