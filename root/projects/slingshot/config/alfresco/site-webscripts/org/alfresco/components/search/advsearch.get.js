/**
 * Advanced Search component GET method
 */

function main()
{
   // fetch the request params required by the advanced search component template
   var siteId = (page.url.templateArgs["site"] != null) ? page.url.templateArgs["site"] : "";
   
   // get the search forms from the config
   var forms = config.scoped["AdvancedSearch"]["advanced-search"].getChild("forms").childrenMap["form"];
   var searchForms = [];
   for (var i = 0, form, formId, label, desc; i < forms.size(); i++)
   {
      form = forms.get(i);
      
      // get optional attributes and resolve label/description text
      formId = form.attributes["id"];
      
      label = form.attributes["label"];
      if (label == null)
      {
         label = form.attributes["labelId"];
         if (label != null)
         {
            label = msg.get(label);
         }
      }
      
      desc = form.attributes["description"];
      if (desc == null)
      {
         desc = form.attributes["descriptionId"];
         if (desc != null)
         {
            desc = msg.get(desc);
         }
      }
      
      // create the model object to represent the form definition
      searchForms.push(
      {
         id: formId ? formId : "search",
         type: form.value,
         label: label ? label : form.value,
         description: desc ? desc : ""
      });
   }
   
   // Prepare the model
   model.siteId = siteId;
   model.searchForms = searchForms;
}

main();