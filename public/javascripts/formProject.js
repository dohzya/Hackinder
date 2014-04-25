/** @jsx React.DOM */

var App = React.createClass({
  getInitialState: function(){
    return { 
      me : $.getJSON("/me")
    };
  },
  render : function(){
    return <div><Notification data={this.state} /><YourProject /><Projects /><Hackers /></div>;
  }
});

var Notification = React.createClass({
  accept : function(e){
    console.log("accept");
    e.preventDefault();
  },
  refuse : function(){
    console.log("refuse");
  },
  render : function(){
    console.log(this.props.data);
    var form = <Participation3/>;
    return <div className="notification">{form}</div>;
  }
});

var Participation1 = React.createClass({
  render : function(){
    return <div className="wrapper participation-1">
        <h1>Veux-tu participer au prochain Hackday ?</h1>
        <p className="details">Une petite réponse avant mercredi 23 avril et on serait ravi !</p>
        <div className="buttons">
                <a href="" className="button polygon" onClick={this.accept}>oh que oui !</a>
                <a href="" onClick={this.refuse}>non</a>
        </div>
    </div>;
  }
});

var Participation2 = React.createClass({
  render : function(){
    return <div className="wrapper participation-2">
        <h1>Très heureux de te compter parmi nous :-)</h1>
        <form>
            <label className="details">Qu'est-ce que tu voudrais faire ?</label>
            <input type="text" placeholder="arduino, fun, café ..."/>
            <input type="submit" className="button polygon" value="GO"/>
        </form>
    </div>;
  }
});

var Participation3 = React.createClass({
  render : function(){
    return <div className="wrapper participation-3">
        <h1>Très heureux de te compter parmi nous :-)</h1>
        <form>
            <label className="details">Qu'est-ce que tu voudrais faire ?</label>
            <input type="text" placeholder="arduino, fun, café ..."/>
            <input type="submit" className="button polygon" value="GO"/>
        </form>
    </div>
  }
});

var YourProject = React.createClass({
  render : function(){
    return <div><formProject/></div>;
  }
});

var Projects = React.createClass({
  render : function(){
    return <div></div>;
  }
});

var Hackers = React.createClass({
  render : function(){
    return <div></div>;
  }
});

var createButton = React.createClass({
  onClick: function () {
    console.log('CLICK', router.currentState().params);
    go({create: true});
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
    go({create: false});
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
  if(create){
    React.renderComponent(<formProject/>, document.getElementById('new-project'));
  }
  else {
    React.renderComponent(<createButton/>, document.getElementById('new-project'));
  }
}


var createApp = function(){
  return React.renderComponent(<App/>, document.getElementById("app"));
};
