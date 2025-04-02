/// <reference types="vite/client" />
declare module "*.vue" {
    import { DefineComponent } from "vue"
    const component: DefineComponent<
        Record<string, unknown>, // props
        Record<string, unknown>, // public properties
        any                     // data
    >
    export default component
}