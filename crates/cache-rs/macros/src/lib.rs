use proc_macro::TokenStream;
use quote::quote;
use syn::{Error, FnArg, ImplItemFn, Pat, Path, parse_macro_input, parse_quote};

/// Read through the registered contract on an async method returning `Result<Option<T>>`.
/// The receiver supplies `self.cache`; all other arguments contribute to the key in order.
#[proc_macro_attribute]
pub fn cache(contract: TokenStream, method: TokenStream) -> TokenStream {
    let contract = parse_macro_input!(contract as Path);
    let mut method = parse_macro_input!(method as ImplItemFn);
    let sig = &method.sig;
    if sig.asyncness.is_none() || sig.receiver().is_none() {
        return Error::new_spanned(sig, "#[cache] requires an async method with a receiver")
            .into_compile_error()
            .into();
    }

    let mut keys = Vec::new();
    for input in &sig.inputs {
        let FnArg::Typed(arg) = input else {
            continue;
        };
        let Pat::Ident(name) = arg.pat.as_ref() else {
            return Error::new_spanned(&arg.pat, "#[cache] arguments must be named")
                .into_compile_error()
                .into();
        };
        let name = &name.ident;
        keys.push(quote!(::cache_rs::CacheArgument::cache_argument(&#name)));
    }
    let body = method.block;
    method.block = parse_quote!({
        self.cache.read((#contract).descriptor, &[#(#keys),*], || async #body).await
    });
    quote!(#method).into()
}
