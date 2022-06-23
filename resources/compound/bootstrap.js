Compound.draw = () => {
  console.log("Compound.data", Compound.data);
  vegaEmbed("#chart", Compound.data);
};

htmx.onLoad(Compound.draw);
