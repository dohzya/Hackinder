/** @jsx React.DOM */

var createButton = React.createClass({
  onClick: function () {
    console.log('CLICK');
  },
  render: function () {
    console.log(this.props);
    return <div class="new-project">
      <button type="button" onClick={this.onClick}>Crée ton projet</button>
    </div>
  }
});

var formProject = React.createClass({
  onSubmit: function () {
    console.log('onSubmit', this.props, this.state);
  },
  handleName: function (event) {
    this.setState({name: event.target.value});
  },
  handleDescription: function (event) {
    this.setState({description: event.target.value});
  },
  handleQuote: function (event) {
    this.setState({quote: event.target.value});
  },
  render: function() {
    if (this.props.data) {
      this.setState(this.props.data);
    }

    return <div class="new-project-2">
      <h1>Crée ton projet</h1>
      <form onSubmit={this.onSubmit}>
        <input type="text" onChange={this.handleName} placeholder="Nom du projet"/>
        <textarea onChange={this.handleDescription} placeholder="Description"/>
        <input type="text" onChange={this.handleQuote} placeholder="Il faut venir dans mon équipe paske..."/>
        <button type="submit">GO !</button>
      </form>
    </div>;
  }
});

var toggleCreate = function (create) {
  if (create) {
    React.renderComponent(<formProject/>, document.getElementById('new-project'));
  } else {
    React.renderComponent(<createButton/>, document.getElementById('new-project'));
  }
}

