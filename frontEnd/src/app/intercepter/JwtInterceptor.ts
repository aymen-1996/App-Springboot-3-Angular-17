import { HttpInterceptorFn } from '@angular/common/http';

export const JwtInterceptor: HttpInterceptorFn = (req, next) => {
    const currentUser = JSON.parse(localStorage.getItem('currentUser') || '{}');
    const authToken = currentUser.token || '';   

    const auth = req.clone({

  });

  if(authToken !==null ){
    const authReq = req.clone({
        setHeaders: {
          Authorization: `Bearer ${authToken}`
        }
      });
    
      return next(authReq);
  }else{
  return next(auth);

  }

};
