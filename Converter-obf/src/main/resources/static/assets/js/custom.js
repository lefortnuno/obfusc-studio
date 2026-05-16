var listItems = [];
$(document).ready(function () {
  var SESSION_TIMEOUT_MS = 15 * 60 * 1000; // 15 minutes
  var inactivityTimer;

  function resetInactivityTimer() {
    clearTimeout(inactivityTimer);
    inactivityTimer = setTimeout(function () {
      setAlert(
        "error",
        "Votre session a expiré. Vous allez être redirigé vers la page de connexion...",
      );
      setTimeout(function () {
        window.location.href = "/login";
      }, 6000); // redirection apres 6s
    }, SESSION_TIMEOUT_MS);
  }

  var lastKeepalive = 0;
  var KEEPALIVE_THROTTLE_MS = 5 * 60 * 1000; // 1 ping serveur max toutes les 5 minutes

  $(document).on("mousemove keydown mousedown touchstart", function () {
    resetInactivityTimer(); // timer JS uniquement, aucun appel réseau
    if ($("#com-structure-form, #sub-structure-form, #add_btn").length > 0) {
      var now = Date.now();
      if (now - lastKeepalive > KEEPALIVE_THROTTLE_MS) {
        lastKeepalive = now;
        $.ajax({ url: "/refresh", type: "GET", global: false });
      }
    }
  });

  resetInactivityTimer();

  $(document).ajaxError(function (event, xhr) {
    if (xhr.status === 401) {
      setAlert(
        "error",
        "Votre session a expiré. Vous allez être redirigé vers la page de connexion...",
      );
      setTimeout(function () {
        window.location.href = "/login";
      }, 5000); // affichage du msp d'erreur pdt 5s
    }
  });

  let field = `<tr class="field-row">
                    <td class="border-bottom-1">
                        <input  type="text" placeholder="nom du champ" min="0" class="form-control field-input" id="field" onkeyup="UpdateListItems(this)" >
                        <span class="text-danger fw-bolder fs-1 input-error" style="display: none"></span>
                    </td>
                    <td class="border-bottom-1">
                        <select class="form-select typefields" name="typefields" id="type01" onchange="checkType(this)">
                            <option value="alfa">text</option>
                            <option value="number">numérique</option>
                        </select>
                        <input type="number" placeholder="taille décimale" min="0" class="form-control mt-2 decimal" id="decimal" style="display: none" >
                        <span class="text-danger fw-bolder fs-1 input-error" style="display: none"></span>
                    </td>
                    <td class="border-bottom-1">
                        <input type="number" placeholder="position" min="0" class="form-control position" id="position"   onchange="checkNumber(this)" >
                        <span class="text-danger fw-bolder fs-1 input-error" style="display: none"></span>
                    </td>
                    <td class="border-bottom-1">
                        <input type="number" placeholder="longeur" min="0" class="form-control longeur" id="longeur"  onchange="checkNumber(this)" >
                        <span class="text-danger fw-bolder fs-1 input-error" style="display: none"></span>
                    </td>
                    <td class="border-bottom-1">
                        <input type="text" placeholder="lien" class="form-control lien" id="lien" onchange="checkempltylink(this)"/>
                        <span class="text-danger fw-bolder fs-1 input-error" style="display: none"></span>
                    </td>
                    <td class="border-bottom-1">
                        <a onclick="removeItem(this)" class="btn btn-link m-1"><i class="ti ti-trash"></i></a>
                    </td>
                 </tr>`;
  let Sub_field = `<tr class="field-row">
                    <td class="border-bottom-1">
                        <input  type="text" placeholder="nom du champ" min="0" class="form-control field-input" id="field" onkeyup="UpdateXmlListItems(this)" >
                        <span class="text-danger fw-bolder fs-1 input-error" style="display: none"></span>
                    </td>
                    <td class="border-bottom-1">
                        <select class="form-select typefields" name="typefields" id="type01" onchange="checkType(this)">
                            <option value="alfa">text</option>
                            <option value="number">numérique</option>
                        </select>
                    </td>
                    <td class="border-bottom-1">
                        <input type="text" placeholder="expression <nom>@nom<\\nom>" class="form-control expression" id="expression"   onchange="checkXML(this)" >
                        <span class="text-danger fw-bolder fs-1 input-error" style="display: none"></span>
                    </td>
                    <td class="border-bottom-1">
                        <a onclick="removeItem(this)" class="btn btn-link m-1"><i class="ti ti-trash"></i></a>
                    </td>
                  </tr>`;
  let com_field = `<tr class="field-row">
                    <td class="border-bottom-1">
                        <select class="form-select typefields" name="typefields" id="type01" onchange="UpdateSUBListItems(this)">
                          <option disabled selected value="">choisir une sous-structure</option>                        
                          $OPTIONS$
                        </select>
                        <span class="text-danger fw-bolder fs-1 input-error" style="display: none"></span>
                    </td>
                    <td class="border-bottom-1">
                        <a onclick="removeItem(this)" class="btn btn-link m-1"><i class="ti ti-trash"></i></a>
                    </td>
                  </tr>`;

  /* complex structure */
  $("#add_com_btn").on("click", function (e) {
    e.preventDefault();
    let options = "";
    $.ajax({
      url: "/complex-structures/main/sub-structure",
      type: "GET",
      success: function (response) {
        response.forEach((substruc) => {
          let value = substruc.id;
          let text = substruc.name;
          let option = `<option value="${value}">${text}</option>`;
          options = options + option;
        });
        let row = com_field.replace("$OPTIONS$", options);
        var item = $(row).clone();
        $("#field_tab").append(item);
      },
      error: function (xhr, status, error) {
        console.log(xhr);
        console.log(status);
        console.log(error);
      },
    });
  });
  $("#gene_com_xml_btn").on("click", function (e) {
    e.preventDefault();
    let struck_name = checkempty("#struc-name");
    if (!struck_name) {
      return;
    }
    let rows = $(".field-row");
    if (rows.length === 0) {
      setAlert("error", "une structure doit avoir au moins une sous-structure");
      return;
    } else {
      $("#alert").hide();
    }
    let big_expression = "";
    for (let index = 0; index < rows.length; index++) {
      const element = rows[index];
      let field_type = $(element).children().find(".typefields")[0];
      if (!UpdateSUBListItems(field_type)) {
        return null;
      }
      var selectedOption = field_type.options[field_type.selectedIndex];
      var selectedText = selectedOption.text;

      big_expression = `${big_expression}   <${selectedText}>@${selectedText}</${selectedText}>\n`;
    }
    $("#big-expression").val(
      `<${struck_name}>\n${big_expression}</${struck_name}>`,
    );
  });
  $("#com-structure-form").on("submit", function (e) {
    e.preventDefault();
    var apiUrl = "/complex-structures/main/store";
    var method = "POST";
    if (e.target.dataset.url) {
      var struck_id = $("#struck-id").val();
      apiUrl = `${e.target.dataset.url}${struck_id}`;
      method = "PUT";
    }
    var csrfToken = $("input[name='_csrf']").val();
    let data = {};
    let struck_name = checkempty("#struc-name");
    if (!struck_name) {
      return;
    }
    let check_name = checkMainName(struck_name, struck_id);
    console.log(check_name);
    if (!check_name) {
      return;
    }
    let struck_lib = checkempty("#lib-struc");
    if (!struck_lib) {
      return;
    }
    let rows = $(".field-row");
    if (rows.length === 0) {
      setAlert("error", "une structure doit avoir au moins une sous-structure");
      return;
    } else {
      $("#alert").hide();
    }
    let list_of_sub_structures = [];
    for (let index = 0; index < rows.length; index++) {
      const element = rows[index];
      let field_type = $(element).children().find(".typefields")[0];
      if (!UpdateSUBListItems(field_type)) {
        return null;
      }
      list_of_sub_structures.push(parseInt($(field_type).val()));
    }
    let big_expression = $("#big-expression").val();
    if (!checkBIGXML("#big-expression")) {
      return;
    }
    data = {
      name: struck_name,
      libelle: struck_lib,
      expression: big_expression,
      subStructuresids: list_of_sub_structures,
    };

    $.ajax({
      url: apiUrl,
      method: method,
      dataType: "json",
      contentType: "application/json",
      beforeSend: function (xhr) {
        xhr.setRequestHeader("X-CSRF-TOKEN", csrfToken);
      },
      data: JSON.stringify(data),
      success: function (data) {
        console.log(data);
        setAlert("success", data.message);
        setTimeout(function () {
          window.location.href = "/complex-structures/main";
        }, 1000);
      },
      error: function (xhr, status, error) {
        if (xhr.status === 401) return;
        console.error("Error:", status, error);
        setAlert(
          "error",
          xhr.responseJSON
            ? xhr.responseJSON.message
            : "Une erreur est survenue.",
        );
      },
    });
  });

  /* sub Structure */
  $("#add_sub_btn").on("click", function (e) {
    e.preventDefault();
    var item = $(Sub_field).clone();
    $("#field_tab").append(item);
  });
  $("#gene_xml_btn").on("click", function (e) {
    e.preventDefault();
    let struck_name = checkempty("#struc-name");
    if (!struck_name) {
      return;
    }
    let rows = $(".field-row");
    if (rows.length === 0) {
      setAlert("error", "une structure doit avoir au moins un champ");
      return;
    } else {
      $("#alert").hide();
    }
    let big_expression = "";
    for (let index = 0; index < rows.length; index++) {
      const element = rows[index];
      let obj = checkXMLRow(element);
      if (obj === null) {
        return;
      }
      big_expression = big_expression + " " + obj.expression + "\n";
    }
    $("#big-expression").val(
      `<${struck_name}>\n${big_expression}</${struck_name}>`,
    );
  });
  $("#sub-structure-form").on("submit", function (e) {
    e.preventDefault();
    var apiUrl = "/complex-structures/sub/store";
    var method = "POST";
    if (e.target.dataset.url) {
      var struck_id = $("#struck-id").val();
      apiUrl = `${e.target.dataset.url}${struck_id}`;
      method = "PUT";
    }
    var csrfToken = $("input[name='_csrf']").val();
    let data = {};
    let struck_name = checkempty("#struc-name");
    if (!struck_name) {
      console.log("error");
      return;
    }
    let check_name = checkSubName(struck_name, struck_id);
    console.log(check_name);
    if (!check_name) {
      console.log("error");
      return;
    }
    let struck_lib = checkempty("#lib-struc");
    if (!struck_lib) {
      console.log("error");
      return;
    }
    let struck_type = checkempty("#struc-type");
    if (!struck_type) {
      console.log("error");
      return;
    }
    let rows = $(".field-row");
    if (rows.length === 0) {
      setAlert("error", "une structure doit avoir au moins un champ");
      return;
    } else {
      $("#alert").hide();
    }
    let fieldlist = [];
    for (let index = 0; index < rows.length; index++) {
      const element = rows[index];
      let obj = checkXMLRow(element);
      if (obj === null) {
        return;
      }
      fieldlist.push(obj);
    }
    let big_expression = $("#big-expression").val();
    if (!checkBIGXML("#big-expression")) {
      return;
    }
    data = {
      name: struck_name,
      libelle: struck_lib,
      type: struck_type,
      expression: big_expression,
      subStructureDetails: fieldlist,
    };

    $.ajax({
      url: apiUrl,
      method: method,
      dataType: "json",
      contentType: "application/json",
      beforeSend: function (xhr) {
        xhr.setRequestHeader("X-CSRF-TOKEN", csrfToken);
      },
      data: JSON.stringify(data),
      success: function (data) {
        console.log(data);
        setAlert("success", data.message);
        setTimeout(function () {
          window.location.href = "/complex-structures/sub";
        }, 1000);
      },
      error: function (xhr, status, error) {
        if (xhr.status === 401) return;
        console.error("Error:", status, error);
        setAlert(
          "error",
          xhr.responseJSON
            ? xhr.responseJSON.message
            : "Une erreur est survenue.",
        );
      },
    });
  });
  /* normal structure */
  $("#add_btn").on("click", function (e) {
    e.preventDefault();
    var item = $(field).clone();
    $("#field_tab").append(item);
  });
  $("#toto").on("submit", function (e) {
    e.preventDefault();
    var apiUrl = "/structurs/store";
    var method = "POST";
    if (e.target.dataset.url) {
      var struck_id = $("#struck-id").val();
      apiUrl = `${e.target.dataset.url}${struck_id}`;
      method = "PUT";
    }
    var csrfToken = $("input[name='_csrf']").val();
    let data = {};
    let struck_name = checkempty("#struc-name");
    if (!struck_name) {
      console.log("struck_name");
      return;
    }
    let check_name = checkName(struck_name, struck_id);
    console.log(check_name);
    if (!check_name) {
      console.log("check_name");
      return;
    }
    let struck_lib = checkempty("#lib-struc");
    if (!struck_lib) {
      console.log("struck_lib");
      return;
    }
    let struck_type = $("#struc-type").val();

    let rows = $(".field-row");
    if (rows.length === 0) {
      setAlert("error", "une structure doit avoir au moins un champ");
      return;
    } else {
      $("#alert").hide();
    }
    let fieldlist = [];
    for (let index = 0; index < rows.length; index++) {
      const element = rows[index];
      let obj = checkRow(element);
      if (obj === null) {
        return;
      }
      fieldlist.push(obj);
    }
    let big_expression = $("#big-expression").val();
    if (!checkXML("#big-expression")) {
      return;
    }
    data = {
      strName: struck_name,
      libelle: struck_lib,
      strType: struck_type,
      structureDetails: fieldlist,
      expression: big_expression,
    };

    $.ajax({
      url: apiUrl,
      method: method,
      dataType: "json",
      contentType: "application/json",
      beforeSend: function (xhr) {
        xhr.setRequestHeader("X-CSRF-TOKEN", csrfToken);
      },
      data: JSON.stringify(data),
      success: function (data) {
        console.log(data);
        setAlert("success", data.message);
        setTimeout(function () {
          window.location.href = "/structurs";
        }, 1000);
      },
      error: function (xhr, status, error) {
        if (xhr.status === 401) return;
        console.error("Error:", status, error);
        setAlert(
          "error",
          xhr.responseJSON
            ? xhr.responseJSON.message
            : "Une erreur est survenue.",
        );
      },
    });
  });
  $("#gene_sim_xml_btn").on("click", function (e) {
    e.preventDefault();
    let struck_name = checkempty("#struc-name");
    if (!struck_name) {
      return;
    }
    let rows = $(".field-row");
    if (rows.length === 0) {
      setAlert("error", "une structure doit avoir au moins un champ");
      return;
    } else {
      $("#alert").hide();
    }
    let big_expression = "";
    for (let index = 0; index < rows.length; index++) {
      const element = rows[index];
      let obj = checkRow(element);
      if (obj === null) {
        return;
      }
      big_expression =
        big_expression + `    <${obj.name}>@${obj.name}</${obj.name}> \n`;
    }
    $("#big-expression").val(`<record>\n${big_expression}</record>`);
  });
  $("#struc-type-in").on("change", (e) => {
    updateSecondSelect(e.target.value);
    $("#meta-csv").hide();
    $("#meta-in").hide();
    if (e.target.value === "xml") {
      $("#meta-lab-in").html("Schéma de fichier");
      $("#meta-in-in").attr("placeholder", "dataset/record");
      $("#meta-in").show();
    } else if (e.target.value === "txt") {
      $("#meta-lab-csv").html("Avec decimal (plat)");
      $("#meta-csv").show();
    } else if (e.target.value === "excel-simp") {
      $("#meta-lab-csv").html("Avec entête (Excel simple)");
      $("#meta-csv").show();
    } else if (e.target.value === "csv") {
      $("#meta-lab-in").html("Séparateur");
      $("#meta-in-in").attr("placeholder", "");
      $("#meta-in").show();

      $("#meta-lab-csv").html("Avec entête (csv)");
      $("#meta-csv").show();
    }
    if (
      e.target.value !== "txt" &&
      e.target.value !== "excel" &&
      e.target.value !== "excel-simp"
    ) {
      $("#meta-in-in").prop("required", true);
    } else {
      $("#meta-in-in").prop("required", false);
    }
  });
  $("#struc-type-out").on("change", (e) => {
    $("#meta-plat").hide();
    $("#meta-out").hide();
    if (e.target.value === "xml") {
      $("#meta-lab-out").html("Schéma de fichier");
      $("#meta-in-out").attr("placeholder", "dataset/record");
      $("#meta-out").show();
    } else if (e.target.value === "txt") {
      $("#meta-lab-plat").html("Avec decimal (plat)");
      $("#meta-plat").show();
    } else if (e.target.value === "csv") {
      $("#meta-lab-out").html("Séparateur");
      $("#meta-in-out").attr("placeholder", "");
      $("#meta-out").show();
      $("#meta-lab-plat").html("Avec entête (csv)");
      $("#meta-plat").show();
    }
    if (e.target.value !== "txt") {
      $("#meta-in-out").prop("required", true);
    } else {
      $("#meta-in-out").prop("required", false);
    }
  });
});

function removeItem(e) {
  e.parentNode.parentNode.parentNode.removeChild(e.parentNode.parentNode);
}
function UpdateListItems(elem) {
  $(elem).val($(elem).val()?.trim());
  if (
    /^[a-zA-Z_][a-zA-Z0-9._-]*$/.test($(elem).val()) &&
    $(elem).val() !== ""
  ) {
    $(elem).parent().find(".input-error").hide();
    $("#btnsubmit").prop("disabled", false);
  } else {
    if ($(elem).val() === "") {
      $(elem)
        .parent()
        .find(".input-error")
        .html("Libellé de champ doit être rempli");
    } else {
      $(elem)
        .parent()
        .find(".input-error")
        .html("Libellé de champ contient des caractères spéciaux");
    }
    $(elem).parent().find(".input-error").show();
    $("#btnsubmit").prop("disabled", true);
    $(elem).parent().parent().find(".expression").val(``);
    return null;
  }
  var inputs = $(".field-input");
  listItems = [];
  for (var i = 0; i < inputs.length; i++) {
    if (!listItems.includes($(inputs[i]).val())) {
      listItems.push($(inputs[i]).val());
    } else {
      $(inputs[i])
        .parent()
        .find(".input-error")
        .html("Libellé de champ deja utiliser");
      $(elem).parent().find(".input-error").show();
      $("#btnsubmit").prop("disabled", true);
      $(elem).parent().parent().find(".expression").val(``);
      return null;
    }
  }
  $(elem)
    .parent()
    .parent()
    .find(".lien")
    .val(`${$(elem).val()}`);
}
function UpdateListItemsSubmit(elem) {
  $(elem).val($(elem).val()?.trim());
  if (
    /^[a-zA-Z_][a-zA-Z0-9._-]*$/.test($(elem).val()) &&
    $(elem).val() !== ""
  ) {
    $(elem).parent().find(".input-error").hide();
    $("#btnsubmit").prop("disabled", false);
  } else {
    if ($(elem).val() === "") {
      $(elem)
        .parent()
        .find(".input-error")
        .html("Libellé de champ doit être rempli");
    } else {
      $(elem)
        .parent()
        .find(".input-error")
        .html("Libellé de champ contient des caractères spéciaux");
    }
    $(elem).parent().find(".input-error").show();
    $("#btnsubmit").prop("disabled", true);
    return null;
  }
  var inputs = $(".field-input");
  listItems = [];
  for (var i = 0; i < inputs.length; i++) {
    if (!listItems.includes($(inputs[i]).val())) {
      listItems.push($(inputs[i]).val());
    } else {
      $(inputs[i])
        .parent()
        .find(".input-error")
        .html("Libellé de champ deja utiliser");
      $(elem).parent().find(".input-error").show();
      $("#btnsubmit").prop("disabled", true);
      return null;
    }
  }
}
function UpdateXmlListItems(elem) {
  console.log("toto");
  $(elem).val($(elem).val()?.trim());
  if (/^[a-zA-Z0-9_éèù ]*$/.test($(elem).val()) && $(elem).val() !== "") {
    $(elem).parent().find(".input-error").hide();
    $("#btnsubmit").prop("disabled", false);
  } else {
    if ($(elem).val() === "") {
      $(elem)
        .parent()
        .find(".input-error")
        .html("Libellé de champ doit être rempli");
    } else {
      $(elem)
        .parent()
        .find(".input-error")
        .html("Libellé de champ contient des caractères spéciaux");
    }
    $(elem).parent().find(".input-error").show();
    $("#btnsubmit").prop("disabled", true);
    $(elem).parent().parent().find(".expression").val(``);
    return null;
  }
  var inputs = $(".field-input");
  listItems = [];
  for (var i = 0; i < inputs.length; i++) {
    if (!listItems.includes($(inputs[i]).val())) {
      listItems.push($(inputs[i]).val());
    } else {
      $(inputs[i])
        .parent()
        .find(".input-error")
        .html("Libellé de champ deja utiliser");
      $(elem).parent().find(".input-error").show();
      $("#btnsubmit").prop("disabled", true);
      $(elem).parent().parent().find(".expression").val(``);
      return null;
    }
  }
  $(elem)
    .parent()
    .parent()
    .find(".expression")
    .val(`<${$(elem).val()}>@${$(elem).val()}</${$(elem).val()}>`);
}
function UpdateSUBListItems(elem) {
  console.log("titi");
  console.log($(elem).val());
  if ($(elem).val() === null) {
    $(elem)
      .parent()
      .find(".input-error")
      .html("une sous-structure doit être choisie");
    $(elem).parent().find(".input-error").show();
    $("#btnsubmit").prop("disabled", true);
    console.log("titi");
    console.log("titi");
    return null;
  }
  var inputs = $(".typefields");
  listItems = [];
  for (var i = 0; i < inputs.length; i++) {
    if (!listItems.includes($(inputs[i]).val())) {
      listItems.push($(inputs[i]).val());
    } else {
      $(inputs[i])
        .parent()
        .find(".input-error")
        .html("cette sous-structure est déjà choisie");
      $(elem).parent().find(".input-error").show();
      $("#btnsubmit").prop("disabled", true);
      return null;
    }
  }
  $(elem).parent().find(".input-error").hide();
  $("#btnsubmit").prop("disabled", false);
  return "toto";
}
function checkType(elem) {
  if ($(elem).val() == "number") {
    $(elem.nextElementSibling).show();
  } else {
    $(elem.nextElementSibling).hide();
  }
}
function checkempty(elem) {
  if ($(elem).val() !== "" && $(elem).val() !== null) {
    $(elem + "-help").hide();
    $("#btnsubmit").prop("disabled", false);
    return $(elem).val();
  } else {
    $(elem + "-help").html("Ce champ ne doit pas être vide");
    $(elem + "-help").show();
    $("#btnsubmit").prop("disabled", true);
    return null;
  }
}
function checkName(value, id) {
  id = id || 0;
  let ajax = true;
  $.ajax({
    url: "/structurs/check-name",
    type: "GET",
    data: {
      name: value,
      id: id,
    },
    success: function (response) {
      ajax = true;
    },
    error: function (xhr) {
      if (xhr.status === 401) return;
      setAlert(
        "error",
        xhr.responseJSON
          ? xhr.responseJSON.message
          : "Une erreur est survenue.",
      );
      ajax = false;
    },
  });
  return ajax;
}
function checkSubName(value, id) {
  id = id || 0;
  let ajax = true;
  $.ajax({
    url: "/complex-structures/sub/check-name", // Replace with the actual endpoint URL
    type: "GET",
    data: {
      name: value,
      id: id,
    },
    success: function (response) {
      ajax = true;
    },
    error: function (xhr) {
      if (xhr.status === 401) return;
      setAlert(
        "error",
        xhr.responseJSON
          ? xhr.responseJSON.message
          : "Une erreur est survenue.",
      );
      ajax = false;
    },
  });
  return ajax;
}
function checkMainName(value, id) {
  id = id || 0;
  let ajax = true;
  $.ajax({
    url: "/complex-structures/main/check-name", // Replace with the actual endpoint URL
    type: "GET",
    data: {
      name: value,
      id: id,
    },
    success: function (response) {
      ajax = true;
    },
    error: function (xhr) {
      if (xhr.status === 401) return;
      setAlert(
        "error",
        xhr.responseJSON
          ? xhr.responseJSON.message
          : "Une erreur est survenue.",
      );
      ajax = false;
    },
  });
  return ajax;
}
function checkRow(elem) {
  let field_id = $(elem).children().find(".row-id");
  let field_name = $(elem).children().find(".field-input");
  if (UpdateListItemsSubmit(field_name)) {
    return null;
  }
  let field_type = $(elem).children().find(".typefields");
  let field_decimal = $(elem).children().find(".decimal");
  if (field_type.val() === "number") {
    if (!checkNumber(field_decimal)) {
      return null;
    }
  }
  let field_posi = $(elem).children().find(".position");
  if (!checkNumber(field_posi)) {
    return null;
  }
  let field_long = $(elem).children().find(".longeur");
  if (!checkNumber(field_long)) {
    return null;
  }
  let field_link = $(elem).children().find(".lien");
  if (!checkempltylink(field_link)) {
    return null;
  }
  return {
    id: field_id.val(),
    name: field_name.val(),
    type: field_type.val(),
    decimal: field_decimal.val(),
    position: field_posi.val(),
    longeur: field_long.val(),
    link: field_link.val(),
  };
}
function checkXMLRow(elem) {
  let field_id = $(elem).children().find(".row-id");
  let field_name = $(elem).children().find(".field-input");
  if (UpdateListItems(field_name)) {
    return null;
  }
  let field_type = $(elem).children().find(".typefields");
  let field_expr = $(elem).children().find(".expression");
  if (!checkXML(field_expr)) {
    return null;
  }

  return {
    id: field_id.val(),
    name: field_name.val(),
    type: field_type.val(),
    expression: field_expr.val(),
  };
}
function checkNumber(elem) {
  if (parseInt($(elem).val()) < 0) {
    $(elem).val(0);
    return true;
  } else if ($(elem).val() !== "") {
    $(elem).parent().find(".input-error").hide();
    $("#btnsubmit").prop("disabled", false);
    return true;
  } else {
    $(elem)
      .parent()
      .find(".input-error")
      .html("Ce champ ne doit pas être vide");
    $(elem).parent().find(".input-error").show();
    $("#btnsubmit").prop("disabled", true);
    return false;
  }
}
function checkempltylink(elem) {
  if ($(elem).val() !== "") {
    $(elem).parent().find(".input-error").hide();
    $("#btnsubmit").prop("disabled", false);
    return true;
  } else {
    $(elem)
      .parent()
      .find(".input-error")
      .html("Ce champ ne doit pas être vide");
    $(elem).parent().find(".input-error").show();
    $("#btnsubmit").prop("disabled", true);
    return false;
  }
}
function checkXML(elem) {
  if ($(elem).val() !== "" && !isValidXML($(elem).val())) {
    $(elem)
      .parent()
      .find(".input-error")
      .html("l'expression xml utilisée n'est pas valide");
    $(elem).parent().find(".input-error").show();
    $("#btnsubmit").prop("disabled", true);
    return false;
  } else if ($(elem).val() !== "") {
    $(elem).parent().find(".input-error").hide();
    $("#btnsubmit").prop("disabled", false);
    return true;
  } else {
    $(elem)
      .parent()
      .find(".input-error")
      .html("Ce champ ne doit pas être vide");
    $(elem).parent().find(".input-error").show();
    $("#btnsubmit").prop("disabled", true);
    return false;
  }
}
function checkBIGXML(elem) {
  if ($(elem).val() !== "" && !isValidXML(`<root>${$(elem).val()}</root>`)) {
    $(elem)
      .parent()
      .find(".input-error")
      .html("l'expression xml utilisée n'est pas valide");
    $(elem).parent().find(".input-error").show();
    $("#btnsubmit").prop("disabled", true);
    return false;
  } else if ($(elem).val() !== "") {
    $(elem).parent().find(".input-error").hide();
    $("#btnsubmit").prop("disabled", false);
    return true;
  } else {
    $(elem)
      .parent()
      .find(".input-error")
      .html("Ce champ ne doit pas être vide");
    $(elem).parent().find(".input-error").show();
    $("#btnsubmit").prop("disabled", true);
    return false;
  }
}
function isValidXML(xmlString) {
  var parser = new DOMParser();
  try {
    var xmlDoc = parser.parseFromString(xmlString, "text/xml");
    // If parsing succeeds and there are no parsing errors, the XML is considered valid
    console.log(xmlDoc.getElementsByTagName("parsererror"));
    return xmlDoc.getElementsByTagName("parsererror").length === 0;
  } catch (error) {
    // If an error occurs during parsing, the XML is considered invalid
    return false;
  }
}
function updateSecondSelect(selectedValue) {
  var select2 = document.getElementById("struc-type-out");
  $("#meta-plat").hide();
  $("#meta-out").hide();
  select2.innerHTML = `<option  selected value="" disabled>choisir le type de sortie</option>`; // Clear existing options

  var options = [
    { value: "xml", text: "XML" },
    { value: "txt", text: "Plat" },
    { value: "csv", text: "CSV" },
  ]; // Define options

  if (selectedValue === "excel") {
    $("#compl-stru").show();
    var optionElement = document.createElement("option");
    optionElement.value = "xml";
    optionElement.textContent = "XML";
    select2.appendChild(optionElement);
    return;
  } else {
    $("#compl-stru").hide();
  }

  // Remove selected option from options array
  options = options.filter((option) => option.value !== selectedValue);

  // Add options to select2
  options.forEach(function (option) {
    var optionElement = document.createElement("option");
    optionElement.value = option.value;
    optionElement.textContent = option.text;
    select2.appendChild(optionElement);
  });
}
function setAlert(type, message) {
  $("#alert").hide();
  if (type === "error") {
    $("#alert").removeClass("alert-success").addClass("alert-danger");
    $("#alert").html(message);
    $("#alert").show();
  } else if (type === "success") {
    $("#alert").addClass("alert-success").removeClass("alert-danger");
    $("#alert").html(message);
    $("#alert").show();
  }
}
function setGet() {
  const typeIn = $("#struc-type-in").val();
  const typeOut = $("#struc-type-out").val();

  if (typeIn === "xml") {
    if (!validateDatasetRecord("#meta-in-in")) {
      event.preventDefault();
      return false;
    }
  }

  if (typeOut === "xml") {
    if (!validateDatasetRecord("#meta-in-out")) {
      event.preventDefault();
      return false;
    }
  }

  $("#index-form").attr("method", "GET");
  $("#file-in").prop("required", false);
}

function setPost() {
  const typeIn = $("#struc-type-in").val();
  const typeOut = $("#struc-type-out").val();

  if (typeIn === "xml") {
    if (!validateDatasetRecord("#meta-in-in")) {
      event.preventDefault();
      return false;
    }
  }

  if (typeOut === "xml") {
    if (!validateDatasetRecord("#meta-in-out")) {
      event.preventDefault();
      return false;
    }
  }

  $("#index-form").attr("method", "POST");
  $("#file-in").prop("required", true);
}

// +++ Nuno
function validateDatasetRecord(inputSelector) {
  const input = document.querySelector(inputSelector);
  const val = input.value.trim();

  // reset message
  input.setCustomValidity("");

  if (!val) {
    input.setCustomValidity("Veuillez renseigner ce champ.");
    input.reportValidity(); // 🔥 affiche le popup comme sur l'image
    return false;
  }

  const parts = val.split("/");

  if (parts.length !== 2 || !parts[0] || !parts[1]) {
    input.setCustomValidity("Format attendu : dataset/record");
    input.reportValidity(); // 🔥 popup natif
    return false;
  }

  return true;
}
