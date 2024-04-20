
export interface User {
  id?:number | null | undefined;
    firstname?: string;
    lastname?: string;
    email?: string;
    orderNumber?: number;

    password?: string;
    token: string  | null;
  }
  